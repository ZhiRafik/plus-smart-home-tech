package ru.yandex.practicum.telemetry.analyzer.service;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public interface ScenarioService {
    void processSnapshot(SensorEventAvro snapshot);
}