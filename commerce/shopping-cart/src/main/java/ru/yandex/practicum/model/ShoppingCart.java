package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "carts")
public class ShoppingCart {
    @Id
    @GeneratedValue
    UUID id;

    @ElementCollection
    @CollectionTable(
            name = "cart_products",
            joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")  // ключ карты = UUID товара
    @Column(name = "quantity", nullable = false) // значение карты = количество
    Map<UUID, Long> products;


    Boolean isActive;

    @Column(name = "username", nullable = false, unique = true)
    String username;
}