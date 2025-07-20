package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioActionId;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioConditionId;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {

    private final SensorRepository sensorRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Получено событие: {}", event);

        DeviceRemovedEventProto payload = event.getDeviceRemoved();
        String sensorId = payload.getId();

        if (sensorId == null || sensorId.isBlank()) {
            log.warn("Получен DEVICE_REMOVED с пустым ID. Пропускаем.");
            return;
        }

        if (!sensorRepository.existsById(sensorId)) {
            log.info("Сенсор с id {} не найден. Нечего удалять.", sensorId);
            return;
        }

        try {
            List<ScenarioCondition> conditions = scenarioConditionRepository.findBySensorId(sensorId);
            scenarioConditionRepository.deleteAll(conditions);

            List<ScenarioAction> actions = scenarioActionRepository.findBySensorId(sensorId);
            scenarioActionRepository.deleteAll(actions);

            sensorRepository.deleteById(sensorId);

            log.info("Сенсор с id {} и все связанные сценарии успешно удалены.", sensorId);
        } catch (Exception e) {
            log.error("Ошибка при удалении сенсора с id {}: {}", sensorId, e.getMessage(), e);
        }
    }
}
