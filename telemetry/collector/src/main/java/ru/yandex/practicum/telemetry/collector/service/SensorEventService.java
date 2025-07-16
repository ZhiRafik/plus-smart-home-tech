package ru.yandex.practicum.telemetry.collector.service;

import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;

public interface SensorEventService {

    void collect(SensorEvent sensorEvent);
}
