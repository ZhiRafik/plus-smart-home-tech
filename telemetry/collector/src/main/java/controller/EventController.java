package controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.hub.HubEvent;
import model.sensor.SensorEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.HubEventService;
import service.SensorEventService;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final SensorEventService sensorEventService;
    private final HubEventService hubEventService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@RequestBody @Valid SensorEvent sensorEvent) {
        sensorEventService.collect(sensorEvent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@RequestBody @Valid HubEvent hubEvent) {
        hubEventService.collect(hubEvent);
        return ResponseEntity.ok().build();
    }
}
