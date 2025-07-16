package ru.yandex.practicum.telemetry.collector.model.sensor;

import ru.yandex.practicum.telemetry.collector.enums.SensorEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class LightSensorEvent extends SensorEvent {

    private final SensorEventType type = SensorEventType.LIGHT_SENSOR_EVENT;
    private Integer luminosity;
    private Integer linkQuality;

    @Override
    public SensorEventType getType() {
        return type;
    }
}