package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

public class ShoppingCartMapper {

    public static ShoppingCartDto mapCartToDto(ShoppingCart cart) {
        return ShoppingCartDto.builder()
                .cartId(cart.getId())
                .products(cart.getProducts())
                .build();
    }
}
