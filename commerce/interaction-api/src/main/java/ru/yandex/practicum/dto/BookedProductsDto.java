package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookedProductsDto {
    @Positive
    Double deliveryWeight;
    @Positive
    Double deliveryVolume;
    Boolean fragile;
}
