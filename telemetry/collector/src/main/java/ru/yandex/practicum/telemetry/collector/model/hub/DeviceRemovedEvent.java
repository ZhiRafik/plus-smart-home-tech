package ru.yandex.practicum.telemetry.collector.model.hub;

import ru.yandex.practicum.telemetry.collector.enums.HubEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceRemovedEvent extends HubEvent {

    @NotBlank
    private String id;

    private final HubEventType type = HubEventType.DEVICE_REMOVED;

    @Override
    public HubEventType getType() {
        return type;
    }
}
