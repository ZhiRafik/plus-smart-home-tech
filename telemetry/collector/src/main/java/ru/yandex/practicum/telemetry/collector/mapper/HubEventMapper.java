package ru.yandex.practicum.telemetry.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.hub.*;

import java.util.List;
import java.util.stream.Collectors;

public class HubEventMapper {

    public static HubEventAvro mapToAvro(HubEvent event) {
        long timestamp = event.getTimestamp().toEpochMilli();
        String hubId = event.getHubId();

        if (event instanceof DeviceAddedEvent e) {
            DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                    .setId(e.getId())
                    .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                    .build();

            return HubEventAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof DeviceRemovedEvent e) {
            DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                    .setId(e.getId())
                    .build();

            return HubEventAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof ScenarioAddedEvent e) {
            List<ScenarioConditionAvro> conditions = e.getConditions().stream()
                    .map(c -> ScenarioConditionAvro.newBuilder()
                            .setSensorId(c.getSensorId())
                            .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                            .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                            .setValue(c.getValue()) // null, int, boolean
                            .build())
                    .collect(Collectors.toList());

            List<DeviceActionAvro> actions = e.getActions().stream()
                    .map(a -> DeviceActionAvro.newBuilder()
                            .setSensorId(a.getSensorId())
                            .setType(ActionTypeAvro.valueOf(a.getType().name()))
                            .setValue(a.getValue()) // nullable int
                            .build())
                    .collect(Collectors.toList());

            ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                    .setName(e.getName())
                    .setConditions(conditions)
                    .setActions(actions)
                    .build();

            return HubEventAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof ScenarioRemovedEvent e) {
            ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                    .setName(e.getName())
                    .build();

            return HubEventAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
    }
}
