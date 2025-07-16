package ru.yandex.practicum.telemetry.collector.service;

import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;

public interface HubEventService {

    void collect(HubEvent hubEvent);
}
