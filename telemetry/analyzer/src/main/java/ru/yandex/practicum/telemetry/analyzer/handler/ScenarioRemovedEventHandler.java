package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        String hubId = event.getHubId();
        String scenarioName = event.getScenarioRemoved().getName();

        Optional<Scenario> optionalScenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);

        if (optionalScenario.isPresent()) {
            Scenario scenario = optionalScenario.get();
            Long scenarioId = scenario.getId();

            try {
                List<ScenarioCondition> conditions = scenarioConditionRepository.findByScenarioId(scenarioId);
                scenarioConditionRepository.deleteAll(conditions);

                List<ScenarioAction> actions = scenarioActionRepository.findByScenarioId(scenarioId);
                scenarioActionRepository.deleteAll(actions);

                scenarioRepository.deleteById(scenarioId);

                log.info("Удалён сценарий '{}' (ID = {}) из хаба {}", scenarioName, scenarioId, hubId);
            } catch (Exception e) {
                log.error("Ошибка при удалении сценария '{}': {}", scenarioName, e.getMessage(), e);
            }
        } else {
            log.warn("Сценарий с именем '{}' для хаба '{}' не найден", scenarioName, hubId);
        }
    }
}
