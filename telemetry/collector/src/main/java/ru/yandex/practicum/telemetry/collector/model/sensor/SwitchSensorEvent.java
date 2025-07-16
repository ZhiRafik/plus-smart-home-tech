package ru.yandex.practicum.telemetry.collector.model.sensor;

import ru.yandex.practicum.telemetry.collector.enums.SensorEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {

    private final SensorEventType type = SensorEventType.SWITCH_SENSOR_EVENT;
    private Boolean state;

    @Override
    public SensorEventType getType() {
        return type;
    }
}