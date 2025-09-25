package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cart_products")
public class ProductCartLink {

    @EmbeddedId
    ProductCartLinkId id;

    @ManyToOne
    @MapsId("cartId")
    @JoinColumn(name = "cart_id",                 // имя колонки FK в cart_products
            referencedColumnName = "cart_id",      // имя PK-колонки в carts
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart")
    )
    ShoppingCart cart;

    Long quantity;

    @Data
    @Builder
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductCartLinkId implements Serializable {
        @Column(name = "cart_id", nullable = false)
        UUID cartId;

        @Column(name = "product_id", nullable = false)
        UUID productId;
    }
}