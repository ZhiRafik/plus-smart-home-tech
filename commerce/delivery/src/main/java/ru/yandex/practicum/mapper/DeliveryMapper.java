package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.model.Delivery;

public final class DeliveryMapper {

    private DeliveryMapper() {}

    public static DeliveryDto toDto(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .fromAddress(delivery.getFromAddress())
                .toAddress(delivery.getToAddress())
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .build();
    }
}
