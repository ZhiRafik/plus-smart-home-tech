package ru.yandex.practicum.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dimension {
    @NotNull @DecimalMin(value = "1.0", inclusive = true)
    Double width;
    @NotNull @DecimalMin(value = "1.0", inclusive = true)
    Double height;
    @NotNull @DecimalMin(value = "1.0", inclusive = true)
    Double depth;
}