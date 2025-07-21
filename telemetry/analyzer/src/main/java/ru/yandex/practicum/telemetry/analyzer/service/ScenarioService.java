package ru.yandex.practicum.telemetry.analyzer.service;

import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;

public interface ScenarioService {

    List<DeviceActionRequest> processSnapshot(SensorsSnapshotAvro snapshot);
}