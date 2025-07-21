package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler<DeviceRemovedEventAvro> {

    private final SensorRepository sensorRepository;

    @Override
    public void handle(DeviceRemovedEventAvro payload, String hubId, Instant timestamp) {
        sensorRepository.deleteById(payload.getId().toString());
    }

    @Override
    public Class<DeviceRemovedEventAvro> getMessageType() {
        return DeviceRemovedEventAvro.class;
    }
}

