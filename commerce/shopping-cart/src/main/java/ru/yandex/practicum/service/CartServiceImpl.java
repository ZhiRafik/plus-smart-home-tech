package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    public ShoppingCartDto addProduct(String username, Map<UUID, Long> products) {
        // 1. Есть ли такая корзина? Если да, добавим туда, иначе создадим новую по имени пользователя
        // 2. Активна ли эта корзина? Если нет, то стоп
        // 3. По найденной корзине по имени пользователя добавляем товар и его количество в связную таблицу
        // 4. Проверить наличие на складе
        Optional<ShoppingCart> cart = cartRepository.findByUsername(username);

        if (cart.isEmpty()) {
            return ShoppingCartMapper.mapCartToDto(
                    ShoppingCart.builder()
                    .isActive(true)
                    .username(username)
                    .products(products)
                    .build()
            );
        } else {

        }
    }

    public ShoppingCartDto getCart(String username) {

    }

    public ResponseEntity<Void> deactivateCart(String username) {

    }

    public ShoppingCartDto removeProducts(String username, List<UUID> products) {

    }

    public ShoppingCartDto changeProductQuantity(String username,
                                                 Map<UUID, Long> productQuantity) {

    }
}
