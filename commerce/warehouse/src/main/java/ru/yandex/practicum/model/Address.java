package ru.yandex.practicum.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {
    String country;
    String city;
    String street;
    String house;
    String flat;

    public static Address of(String token) {
        return Address.builder()
                .country(token)
                .city(token)
                .street(token)
                .house(token)
                .flat(token)
                .build();
    }
}
