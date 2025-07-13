package model.sensor;

import enums.SensorEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {

    private final SensorEventType type = SensorEventType.CLIMATE_SENSOR_EVENT;
    private Integer temperatureC;
    private Integer humidity;
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return type;
    }
}

