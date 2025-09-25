package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "warehouses")
public class Warehouse {
    @Id
    @GeneratedValue
    UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "address_country")),
            @AttributeOverride(name = "city",    column = @Column(name = "address_city")),
            @AttributeOverride(name = "street",  column = @Column(name = "address_street")),
            @AttributeOverride(name = "house",   column = @Column(name = "address_house")),
            @AttributeOverride(name = "flat",    column = @Column(name = "address_flat"))
    })
    Address address;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    List<WarehouseItem> items;
}