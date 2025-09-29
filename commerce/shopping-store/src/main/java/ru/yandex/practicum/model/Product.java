package ru.yandex.practicum.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue
    UUID productId;

    @NotBlank
    @Size(max = 255)
    String productName;

    String description;

    String imageSrc;

    @NotNull
    @Enumerated(EnumType.STRING)
    QuantityState quantityState;

    @NotNull
    @Enumerated(EnumType.STRING)
    ProductState productState;

    @NotNull
    @Enumerated(EnumType.STRING)
    ProductCategory productCategory;

    @NotNull
    @Positive
    Double price;
}
