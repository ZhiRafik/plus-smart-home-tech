package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {

    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioRepository scenarioRepository;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        ScenarioAddedEventProto payload = event.getScenarioAdded();

        Scenario scenario = new Scenario();
        scenario.setHubId(event.getHubId().toString());
        scenario.setName(payload.getName());

        for (var protoCond : payload.getConditionList()) {
            Condition condition = new Condition();
            condition.setType(protoCond.getType().name());
            condition.setOperation(protoCond.getOperation().name());
            condition.setValue(protoCond.getBoolValue() ? 1 : 0); // boolean → int

            Sensor sensor = new Sensor();
            sensor.setId(String.valueOf(UUID.fromString(protoCond.getSensorId())));

            ScenarioCondition scenarioCondition = new ScenarioCondition();
            scenarioCondition.setScenario(scenario);
            scenarioCondition.setSensor(sensor);
            scenarioCondition.setCondition(condition);

            scenarioConditionRepository.save(scenarioCondition);
        }


        for (var protoAction : payload.getActionList()) {
            Action action = new Action();
            action.setType(protoAction.getType().name());
            action.setValue(protoAction.getValue());
            action.setSensorId(protoAction.getSensorId().toString());
            action.setScenario(scenario); // установим связь

            actionRepository.save(action);
        }

        scenarioRepository.save(scenario);
        log.info("Добавлен сценарий: {}", scenario.getName());
    }
}
