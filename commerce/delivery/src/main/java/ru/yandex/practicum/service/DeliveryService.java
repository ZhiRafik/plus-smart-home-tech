package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;

import java.util.UUID;

public interface DeliveryService {

    DeliveryDto planDelivery(DeliveryDto request); // create

    void markPicked(UUID orderId); // IN_PROGRESS + вызовы внешних сервисов

    void markSuccessful(UUID orderId); // DELIVERED + статус заказа DELIVERED

    void markFailed(UUID orderId); // FAILED + статус заказа DELIVERY_FAILED

    double calculateCost(OrderDto order); // по ТЗ
}
