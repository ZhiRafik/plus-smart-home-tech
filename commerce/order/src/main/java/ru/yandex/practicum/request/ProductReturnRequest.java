package ru.yandex.practicum.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductReturnRequest {

    UUID orderId;

    HashMap<UUID, Long> products; // productId -> quantity;
}
