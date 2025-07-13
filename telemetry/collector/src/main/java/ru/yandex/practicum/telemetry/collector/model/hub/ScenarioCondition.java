package ru.yandex.practicum.telemetry.collector.model.hub;

import ru.yandex.practicum.telemetry.collector.enums.ConditionType;
import ru.yandex.practicum.telemetry.collector.enums.ConditionOperation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioCondition {

    @NotBlank
    private String sensorId;

    @NotNull
    private ConditionType type;

    @NotNull
    private ConditionOperation operation;

    private Object value;
}
