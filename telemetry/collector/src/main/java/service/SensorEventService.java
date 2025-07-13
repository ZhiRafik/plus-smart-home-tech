package service;

import model.sensor.SensorEvent;

public interface SensorEventService {

    void collect(SensorEvent sensorEvent);
}
