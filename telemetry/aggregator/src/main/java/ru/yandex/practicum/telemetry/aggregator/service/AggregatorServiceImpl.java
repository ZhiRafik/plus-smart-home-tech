package ru.yandex.practicum.telemetry.aggregator.service;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.*;

public class AggregatorServiceImpl implements AggregatorService {

    private final Map<String, SensorsSnapshotAvro> snapshotByHub = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId().toString();
        String sensorId = event.getId().toString();
        long eventTimestamp = event.getTimestamp();

        // 1. Получаем или создаём снапшот хаба
        SensorsSnapshotAvro snapshot = snapshotByHub.get(hubId);
        if (snapshot == null) {
            snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(hubId);
            snapshot.setSensorsState(new HashMap<>());
            snapshotByHub.put(hubId, snapshot);
        }

        // 2. Получаем карту состояний сенсоров
        Map<String, SensorStateAvro> states = new HashMap<>();
        for (Map.Entry<CharSequence, SensorStateAvro> entry : snapshot.getSensorsState().entrySet()) {
            states.put(entry.getKey().toString(), entry.getValue());
        }
        SensorStateAvro oldState = states.get(sensorId);

        // 3. Если состояние уже есть — проверим, нужно ли обновлять
        if (oldState != null) {
            long oldTs = oldState.getTimestamp().toEpochMilli();

            if (oldTs >= eventTimestamp) {
                return Optional.empty(); // событие устарело
            }

            if (oldState.getData().equals(event.getPayload())) {
                return Optional.empty(); // данные не изменились
            }
        }

        // 4. Создаём новое состояние сенсора
        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(Instant.ofEpochMilli(eventTimestamp));
        newState.setData(event.getPayload());

        // 5. Обновляем Map
        states.put(sensorId, newState);

        // 6. Обновляем таймстемп снапшота
        snapshot.setTimestamp(Instant.ofEpochMilli(eventTimestamp));

        return Optional.of(snapshot);
    }

}
