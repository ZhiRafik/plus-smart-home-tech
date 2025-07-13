package model.hub;

import enums.DeviceType;
import enums.HubEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {

    @NotBlank
    private String id;

    @NotNull
    private DeviceType deviceType;

    private final HubEventType type = HubEventType.DEVICE_ADDED;

    @Override
    public HubEventType getType() {
        return type;
    }
}
