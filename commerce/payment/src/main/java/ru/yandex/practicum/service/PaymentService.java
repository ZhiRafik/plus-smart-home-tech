package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.util.UUID;

public interface PaymentService {

    PaymentDto createPayment(OrderDto order);

    double calculateProductCost(OrderDto order);

    double calculateTotalCost(OrderDto order);

    void refund(UUID paymentId);

    void paymentFailed(UUID paymentId);
}