package ru.yandex.practicum.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippedToDeliveryRequest {

    UUID orderId;

    UUID deliveryId;
}