package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.util.Timestamps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
    public void processSnapshot(SensorEventAvro snapshot) {
        String hubId = snapshot.getHubId().toString();
        log.info("Обработка снапшота для хаба: {}", hubId);

        Map<String, Integer> sensorValues = extractSensorValues(snapshot);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.debug("Найдено {} сценариев для хаба {}", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            List<ScenarioCondition> conditions = scenarioConditionRepository.findByScenarioId(scenario.getId());

            boolean allConditionsTrue = conditions.stream().allMatch(sc -> {
                String sensorId = sc.getSensor().getId();
                Integer currentValue = sensorValues.get(sensorId);

                if (currentValue == null) {
                    log.warn("Нет значения для сенсора {}", sensorId);
                    return false;
                }

                return evaluateCondition(currentValue, sc.getCondition());
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

    private Map<String, Integer> extractSensorValues(SensorEventAvro snapshot) {
        Map<String, Integer> result = new HashMap<>();

        Object payload = snapshot.getPayload();
        String sensorId = snapshot.getId().toString();

        if (payload instanceof TemperatureSensorAvro temp) {
            result.put(sensorId, (int) temp.getTemperatureC());
        } else if (payload instanceof ClimateSensorAvro climate) {
            result.put(sensorId, (int) climate.getHumidity()); // или temp.getTemperature()
        } else if (payload instanceof LightSensorAvro light) {
            result.put(sensorId, (int) light.getLuminosity());
        } else if (payload instanceof MotionSensorAvro motion) {
            result.put(sensorId, motion.getMotion() ? 1 : 0);
        } else if (payload instanceof SwitchSensorAvro sw) {
            result.put(sensorId, sw.getState() ? 1 : 0);
        } else {
            log.warn("Неизвестный тип сенсора: {}", payload.getClass().getSimpleName());
        }

        return result;
    }

    private boolean evaluateCondition(Integer currentValue, Condition condition) {
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
