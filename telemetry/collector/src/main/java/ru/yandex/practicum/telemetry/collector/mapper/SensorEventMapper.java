package ru.yandex.practicum.telemetry.collector.mapper;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.sensor.*;

public class SensorEventMapper {

    public static @NotNull SensorEventAvro mapToAvro(@NotNull SensorEvent event) {
        long timestamp = event.getTimestamp().toEpochMilli();
        String id = event.getId();
        String hubId = event.getHubId();

        if (event instanceof TemperatureSensorEvent e) {
            TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setTemperatureF(e.getTemperatureF())
                    .build();

            return SensorEventAvro.newBuilder()
                    .setId(id)
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof LightSensorEvent e) {
            LightSensorAvro payload = LightSensorAvro.newBuilder()
                    .setLuminosity(e.getLuminosity())
                    .setLinkQuality(e.getLinkQuality())
                    .build();

            return SensorEventAvro.newBuilder()
                    .setId(id)
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof MotionSensorEvent e) {
            MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                    .setMotion(e.getMotion())
                    .setLinkQuality(e.getLinkQuality())
                    .setVoltage(e.getVoltage())
                    .build();

            return SensorEventAvro.newBuilder()
                    .setId(id)
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof SwitchSensorEvent e) {
            SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                    .setState(e.getState())
                    .build();

            return SensorEventAvro.newBuilder()
                    .setId(id)
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        if (event instanceof ClimateSensorEvent e) {
            ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setHumidity(e.getHumidity())
                    .setCo2Level(e.getCo2Level())
                    .build();

            return SensorEventAvro.newBuilder()
                    .setId(id)
                    .setHubId(hubId)
                    .setTimestamp(timestamp)
                    .setPayload(payload)
                    .build();
        }

        throw new IllegalArgumentException("Unsupported sensor type: " + event.getClass().getName());
    }
}
