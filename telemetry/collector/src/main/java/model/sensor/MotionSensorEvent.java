package model.sensor;

import enums.SensorEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent {

    private final SensorEventType type = SensorEventType.MOTION_SENSOR_EVENT;
    private Integer voltage;
    private Integer linkQuality;
    private Boolean motion;

    @Override
    public SensorEventType getType() {
        return type;
    }
}