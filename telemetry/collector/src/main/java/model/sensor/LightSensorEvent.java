package model.sensor;

import enums.SensorEventType;
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