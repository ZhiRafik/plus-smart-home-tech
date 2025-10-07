package ru.yandex.practicum.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.OrderState;

import java.util.HashMap;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    UUID orderId;
    UUID shoppingCartId;
    HashMap<UUID, Integer> products; // ID -> quantity
    UUID paymentId;
    UUID deliveryId;
    OrderState state;
    Double deliveryWeight;
    Double deliveryVolume;
    Boolean fragile;
    Double totalPrice;
    Double deliveryPrice;
    Double productPrice;
}
