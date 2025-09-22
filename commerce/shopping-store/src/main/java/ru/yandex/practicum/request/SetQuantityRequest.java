package ru.yandex.practicum.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetQuantityRequest {
    UUID productId;

    QuantityState quantityState;
}
