package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class AggregatorServiceImpl implements AggregatorService {

    private final Map<String, SensorsSnapshotAvro> snapshotByHub = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId().toString();
        CharSequence sensorId = event.getId();
        long eventTimestamp = event.getTimestamp();

        // 1. Получаем или создаём снапшот хаба
        SensorsSnapshotAvro snapshot = snapshotByHub.get(hubId);
        if (snapshot == null) {
            snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(hubId);
            snapshot.setSensorsState(new HashMap<>());  // Инициализация
            snapshotByHub.put(hubId, snapshot);
        }

        // 2. Работаем напрямую с Map<CharSequence, SensorStateAvro>
        Map<CharSequence, SensorStateAvro> states = snapshot.getSensorsState();
        SensorStateAvro oldState = states.get(sensorId);

        // 3. Если состояние уже есть — проверим, нужно ли обновлять
        if (oldState != null) {
            long oldTs = oldState.getTimestamp().toEpochMilli();
            if (oldTs >= eventTimestamp) {
                log.info("Пропускаю событие от сенсора {}: устаревшее ({} <= {})", sensorId, eventTimestamp, oldTs);
                return Optional.empty();
            }
            if (oldState.getData() != null && event.getPayload() != null
                    && oldState.getData().toString().equals(event.getPayload().toString())) {
                log.info("Пропускаю событие от сенсора {}: данные не изменились", sensorId);
                return Optional.empty();
            }
        }

        // 4. Создаём новое состояние сенсора
        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(Instant.ofEpochMilli(eventTimestamp));
        newState.setData(event.getPayload());

        // 5. Обновляем Map прямо в snapshot
        states.put(sensorId, newState);

        // 6. Обновляем timestamp снапшота
        snapshot.setTimestamp(Instant.ofEpochMilli(eventTimestamp));

        return Optional.of(snapshot);
    }
}
