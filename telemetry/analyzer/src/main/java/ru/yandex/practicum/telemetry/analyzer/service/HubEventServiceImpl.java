package ru.yandex.practicum.telemetry.analyzer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.handler.HubEventDispatcher;

@Service
@RequiredArgsConstructor
public class HubEventServiceImpl implements HubEventService {

    private final HubEventDispatcher hubEventDispatcher;

    @Transactional
    public void processEvent(HubEventAvro hubEventavro) {
        hubEventDispatcher.dispatch(hubEventavro);
    }

}
