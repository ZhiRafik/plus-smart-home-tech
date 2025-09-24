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
/* пока метод не нужен
    public static ShoppingCart mapDtoToCart(ShoppingCartDto dto) {
        return ShoppingCart.builder()
                .cartId(dto.getCartId())
                .isActive(найти в базе)
                .username(найти в базе)
                .products(dto.getProducts())
                .build();
    }
 */
}
