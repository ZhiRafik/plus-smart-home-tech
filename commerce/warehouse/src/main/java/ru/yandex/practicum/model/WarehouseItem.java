package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "warehouses_items")
public class WarehouseItem {
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    UUID id;

    @Column(name = "product_id", nullable = false, columnDefinition = "uuid")
    UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, columnDefinition = "uuid")
    Warehouse warehouse;

    @Embedded
    Dimension dimension;

    Boolean fragile;

    @Column(nullable = false)
    Long quantity;

    Double weight;
}