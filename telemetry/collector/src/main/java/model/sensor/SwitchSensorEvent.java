package model.sensor;

import enums.SensorEventType;
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