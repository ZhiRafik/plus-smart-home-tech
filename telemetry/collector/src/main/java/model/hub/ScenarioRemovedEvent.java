package model.hub;

import enums.HubEventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {

    @NotBlank
    private String name;

    private final HubEventType type = HubEventType.SCENARIO_REMOVED;

    @Override
    public HubEventType getType() {
        return type;
    }
}
