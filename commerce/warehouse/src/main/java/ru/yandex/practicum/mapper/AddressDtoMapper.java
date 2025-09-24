package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.model.Address;

public class AddressDtoMapper {

    public static AddressDto mapToDto(Address address) {
        return AddressDto.builder()
                .city(address.getCity())
                .country(address.getCountry())
                .flat(address.getFlat())
                .house(address.getHouse())
                .street(address.getStreet())
                .build();
    }
}
