package ru.yandex.practicum.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

    String findUsernameByCartId(UUID cartId);
}
