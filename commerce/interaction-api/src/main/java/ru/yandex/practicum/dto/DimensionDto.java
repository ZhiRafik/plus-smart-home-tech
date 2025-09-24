package ru.yandex.practicum.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DimensionDto {
    @NotNull @DecimalMin(value = "1.0", inclusive = true)
    Double width;
    @NotNull @DecimalMin(value = "1.0", inclusive = true)
    Double height;
    @NotNull @DecimalMin(value = "1.0", inclusive = true)
    Double depth;
}
