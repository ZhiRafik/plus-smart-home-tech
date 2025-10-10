package ru.yandex.practicum.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.DeliveryState;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryDto {

    UUID deliveryId;

    AddressDto fromAddress;

    AddressDto toAddress;

    UUID orderId;

    DeliveryState deliveryState;
}
