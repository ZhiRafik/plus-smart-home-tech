package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDto {

    UUID paymentId;

    @Positive
    Double totalPayment;

    @Positive
    Double deliveryTotal;

    @Positive
    Double feeTotal;
}
