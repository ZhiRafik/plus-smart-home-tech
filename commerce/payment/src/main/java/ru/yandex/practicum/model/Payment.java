package ru.yandex.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.PaymentStatus;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @Column(name = "id", nullable = false)
    UUID paymentId;

    UUID orderId;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @Positive
    Double totalPayment;

    @Positive
    Double deliveryTotal;

    @Positive
    Double feeTotal;
}
