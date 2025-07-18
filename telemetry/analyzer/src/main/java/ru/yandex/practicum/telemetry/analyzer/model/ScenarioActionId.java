package ru.yandex.practicum.telemetry.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioActionId implements Serializable {
    private Long scenario;
    private String sensor;
    private Long action;
}

