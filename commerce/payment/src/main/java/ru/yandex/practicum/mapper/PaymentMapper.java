package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.model.Payment;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDto.builder()
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .totalPayment(payment.getTotalPayment())
                .paymentId(payment.getPaymentId())
                .build();
    }
}
