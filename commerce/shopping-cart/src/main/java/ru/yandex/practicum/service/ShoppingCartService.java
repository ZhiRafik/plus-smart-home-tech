package ru.yandex.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto addProduct(String username, Map<UUID, Long> products);

    ShoppingCartDto getCart(String username);

    ResponseEntity<Void> deactivateCart(String username);

    ShoppingCartDto removeProducts(String username, List<UUID> products);

    ShoppingCartDto changeProductQuantity(String username, Map<Long, UUID> productsQuantity);
}
