package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {

    private final SensorRepository sensorRepository;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Получено событие: {}", event);

        DeviceAddedEventProto payload = event.getDeviceAdded();
        if (payload.getId() == null || payload.getId().isBlank()) {
            log.warn("Получен DEVICE_ADDED с пустым ID. Пропускаем.");
            return;
        }


        if (sensorRepository.existsById(payload.getId())) {
            log.info("Сенсор с id {} уже существует. Пропускаем добавление.", payload.getId());
            return;
        }

        try {
            Sensor sensor = new Sensor();
            sensor.setHubId(event.getHubId());
            sensor.setId(payload.getId());
            sensorRepository.save(sensor);
            log.info("Сенсор добавлен: {}", sensor);
        } catch (Exception e) {
            log.error("Ошибка при сохранении сенсора: {}", e.getMessage(), e);
        }

    }
}
