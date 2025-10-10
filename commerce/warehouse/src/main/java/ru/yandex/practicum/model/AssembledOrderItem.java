package ru.yandex.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "assembled_orders_items")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssembledOrderItem {
    @Id @GeneratedValue
    UUID id;

    @NotNull
    UUID orderId;

    UUID deliveryId;

    @NotNull
    UUID productId;

    @NotNull
    Long quantity;
}
