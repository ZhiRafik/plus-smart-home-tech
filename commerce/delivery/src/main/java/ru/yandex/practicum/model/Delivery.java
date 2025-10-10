package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.enums.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "delivery")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Delivery {
    @Id
    @GeneratedValue
    UUID deliveryId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country")),
            @AttributeOverride(name = "city",    column = @Column(name = "from_city")),
            @AttributeOverride(name = "street",  column = @Column(name = "from_street")),
            @AttributeOverride(name = "house",   column = @Column(name = "from_house")),
            @AttributeOverride(name = "flat",    column = @Column(name = "from_flat"))
    })
    AddressDto fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city",    column = @Column(name = "to_city")),
            @AttributeOverride(name = "street",  column = @Column(name = "to_street")),
            @AttributeOverride(name = "house",   column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat",    column = @Column(name = "to_flat"))
    })
    AddressDto toAddress;

    @Column(nullable = false)
    UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    DeliveryState deliveryState; 

    Double totalWeight;

    Double totalVolume;

    Boolean fragile;
}

