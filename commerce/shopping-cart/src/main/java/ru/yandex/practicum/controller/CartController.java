package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController {
    CartService cartService;
    private final static String BLANK_NAME = "Имя пользователя не должно быть пустым";
    // нужно, чтобы возвращалось 401

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam @NotBlank(message = BLANK_NAME) String username,
                                      @RequestBody Map<UUID, Long> products) {
        return cartService.addProduct(username, products);
    }

    @GetMapping
    public ShoppingCartDto getCart(@RequestParam @NotBlank(message = BLANK_NAME) String username) {
        return cartService.getCart(username);
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateCart(@RequestParam @NotBlank(message = BLANK_NAME) String username) {
        return cartService.deactivateCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(@RequestParam @NotBlank(message = BLANK_NAME) String username,
                                          @RequestBody List<UUID> products) {
        return cartService.removeProducts(username, products);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam @NotBlank(message = BLANK_NAME) String username,
                                                 @RequestBody Map<UUID, Long> productsQuantity) {
        return cartService.changeProductQuantity(username, productsQuantity);
    }
}
