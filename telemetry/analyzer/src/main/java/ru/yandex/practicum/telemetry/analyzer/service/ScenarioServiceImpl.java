package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.util.Timestamps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.Schema;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.event.enums.ActionTypeProto;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.grpc.GrpcCommandSender;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioServiceImpl implements ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final GrpcCommandSender grpcCommandSender;

    @Override
    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId().toString();
        log.info("Обработка снапшота для хаба: {}", hubId);

        Map<String, Map<String, Integer>> sensorValues = extractSensorValues(snapshot);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.debug("Найдено {} сценариев для хаба {}", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            List<ScenarioCondition> conditions = scenarioConditionRepository.findByScenarioId(scenario.getId());

            boolean allConditionsTrue = conditions.stream().allMatch(sc -> {
                String sensorId = sc.getSensor().getId();
                Map<String, Integer> currentValues = sensorValues.get(sensorId);

                if (currentValues == null) {
                    log.warn("Нет значения для сенсора {}", sensorId);
                    return false;
                }

                return evaluateCondition(currentValues, sc.getCondition());
            });

            if (allConditionsTrue) {
                log.info("Сценарий '{}' активирован", scenario.getName());
                List<ScenarioAction> actions = scenarioActionRepository.findByScenarioId(scenario.getId());
                actions.forEach(action -> {
                    DeviceActionRequest request = DeviceActionRequest.newBuilder()
                            .setHubId(hubId)
                            .setScenarioName(scenario.getName())
                            .setAction(DeviceActionProto.newBuilder()
                                    .setType(ActionTypeProto.valueOf(action.getAction().getType()))
                                    .setValue(action.getAction().getValue())
                                    .build())
                            .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                            .build();

                    grpcCommandSender.sendDeviceAction(request);

                    log.info("Отправлена gRPC-команда: {}", request);
                });
            } else {
                log.info("Сценарий '{}' не активирован", scenario.getName());
            }
        }
    }
    private Map<String, Map<String, Integer>> extractSensorValues(SensorsSnapshotAvro snapshot) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for (Map.Entry<CharSequence, SensorStateAvro> entry : snapshot.getSensorsState().entrySet()) {
            String sensorId = entry.getKey().toString();
            Object payload = entry.getValue().getData();

            if (!(payload instanceof SpecificRecordBase record)) {
                log.warn("Payload не является Avro-рекордом: {}", payload.getClass().getSimpleName());
                continue;
            }

            Map<String, Integer> sensorMetrics = new HashMap<>();

            Schema schema = record.getSchema();
            for (Schema.Field field : schema.getFields()) {
                String fieldName = field.name();
                Object rawValue = record.get(field.pos());

                if (rawValue instanceof Boolean b) {
                    sensorMetrics.put(fieldName, b ? 1 : 0);
                } else if (rawValue instanceof Number n) {
                    sensorMetrics.put(fieldName, n.intValue());
                } else {
                    log.debug("Поле {} не добавлено: не является числом или boolean (тип: {})",
                            fieldName, rawValue != null ? rawValue.getClass().getSimpleName() : "null"
                    );
                }
            }

            result.put(sensorId, sensorMetrics);
        }

        return result;
    }

    private boolean evaluateCondition(Map<String, Integer> currentValue, Condition condition) {
        return switch (condition.getOperation()) {
            case "БОЛЬШЕ" -> currentValue > condition.getValue();
            case "МЕНЬШЕ" -> currentValue < condition.getValue();
            case "РАВНО" -> currentValue.equals(condition.getValue());
            default -> {
                log.warn("Неизвестная операция '{}'", condition.getOperation());
                yield false;
            }
        };
    }
}
