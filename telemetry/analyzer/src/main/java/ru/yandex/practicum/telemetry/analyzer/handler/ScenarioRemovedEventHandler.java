package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionRepository conditionRepository;
    private final ScenarioActionRepository actionRepository;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        String hubId = event.getHubId().toString();
        String scenarioName = event.getScenarioRemoved().getName();

        Optional<Scenario> optionalScenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);

        if (optionalScenario.isPresent()) {
            Scenario scenario = optionalScenario.get();
            Long scenarioId = scenario.getId();

            conditionRepository.deleteBy(scenarioId);
            actionRepository.deleteByScenarioId(scenarioId);
            scenarioRepository.deleteById(scenarioId);

            log.info("Удалён сценарий '{}' (ID = {}) из хаба {}", scenarioName, scenarioId, hubId);
        } else {
            log.warn("Сценарий с именем '{}' для хаба '{}' не найден", scenarioName, hubId);
        }
    }

}
