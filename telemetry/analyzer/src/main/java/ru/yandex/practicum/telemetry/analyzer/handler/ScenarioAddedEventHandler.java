package ru.practicum.service.handler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.handler.HubEventHandler;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEventAvro> {

    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioConditionLinkRepository scenarioConditionLinkRepository;
    private final ScenarioActionLinkRepository scenarioActionLinkRepository;

    @Transactional
    @Override
    public void handle(ScenarioAddedEventAvro payload, String hubId, Instant timestamp) {
        Scenario scenarioEntity = new Scenario();
        scenarioEntity.setHubId(hubId);
        scenarioEntity.setName(payload.getName().toString());

        log.info("Save ScenarioAddedEventAvro: {}", payload);
        log.info("hubId: {}", hubId);
        log.info("timestamp: {}", timestamp);


        List<Condition> conditionEntityList = payload.getConditions().stream()
                .map(avroCondition -> Condition.builder()
                        .type(avroCondition.getType().toString())
                        .value(switch (avroCondition.getValue()) {
                            case null -> null;
                            case Integer intValue -> intValue;
                            case Boolean boolValue -> boolValue ? 1 : 0;
                            default -> throw new IllegalArgumentException(
                                    "Unsupported value type: " + avroCondition.getValue().getClass());
                        })
                        .operation(avroCondition.getOperation().toString())
                        .build())
                .toList();

        List<ScenarioConditionLink> conditionLinks = payload.getConditions().stream()
                .map(avroCondition -> {

                    String sensorId = avroCondition.getSensorId().toString();
                    Sensor sensor = Sensor.builder()
                            .id(sensorId)
                            .hubId(hubId)
                            .build();

                    Condition condition = conditionEntityList.get(payload.getConditions().indexOf(avroCondition));

                    return ScenarioConditionLink.builder()
                            .id(new ScenarioConditionLink.ScenarioConditionId(
                                    scenarioEntity.getId(),
                                    sensor.getId(),
                                    condition.getId()
                            ))
                            .scenario(scenarioEntity)
                            .sensor(sensor)
                            .condition(condition)
                            .build();
                })
                .toList();

        List<Action> actionEntityList = payload.getActions().stream()
                .map(avroAction -> Action.builder()
                        .type(avroAction.getType().toString())
                        .value(avroAction.getValue())
                        .build())
                .toList();

        List<ScenarioActionLink> actionLinks = payload.getActions().stream()
                .map(avroAction -> {
                    String sensorId = avroAction.getSensorId().toString();
                    Sensor sensor = Sensor.builder()
                            .id(sensorId)
                            .hubId(hubId)
                            .build();
                    Action Action = actionEntityList.get(payload.getActions().indexOf(avroAction));
                    return ScenarioActionLink.builder()
                            .id(new ScenarioActionLink.ScenarioActionId(
                                    scenarioEntity.getId(),
                                    sensor.getId(),
                                    Action.getId()
                            ))
                            .scenario(scenarioEntity)
                            .sensor(sensor)
                            .action(Action)
                            .build();
                })
                .toList();

        log.info("Save entity: {}", scenarioEntity);
        scenarioRepository.save(scenarioEntity);

        log.info("Save conditionList: {}", conditionEntityList);
        conditionRepository.saveAll(conditionEntityList);

        log.info("Save action: {}", actionEntityList);
        actionRepository.saveAll(actionEntityList);

        log.info("Save condition links: {}", conditionLinks);
        scenarioConditionLinkRepository.saveAll(conditionLinks);

        log.info("Save action links: {}", actionLinks);
        scenarioActionLinkRepository.saveAll(actionLinks);

    }

    @Override
    public Class<ScenarioAddedEventAvro> getMessageType() {
        return ScenarioAddedEventAvro.class;
    }
}