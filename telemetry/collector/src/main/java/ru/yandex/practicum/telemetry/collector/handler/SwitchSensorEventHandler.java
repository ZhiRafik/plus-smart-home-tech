package ru.yandex.practicum.telemetry.collector.handler;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@Component
public class SwitchSensorEventHandler implements SensorEventHandler {
    // ...детали реализации опущены...

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        // ...детали реализации опущены...
    }
}
