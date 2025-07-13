package service;

import model.hub.HubEvent;

public interface HubEventService {

    void collect(HubEvent hubEvent);
}
