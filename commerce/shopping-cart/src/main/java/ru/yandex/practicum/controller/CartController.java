package ru.yandex.practicum.controller;

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

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam String username, @RequestBody Map<UUID, Long> products) {
        return cartService.addProduct(username, products);
    }

    @GetMapping
    public ShoppingCartDto getCart(@RequestParam String username) {
        return cartService.getCart(username);
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateCart(@RequestParam String username) {
        return cartService.deactivateCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(@RequestParam String username, @RequestBody List<UUID> products) {
        return cartService.removeProducts(username, products);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam String username,
                                                 @RequestBody Map<UUID, Long> productQuantity) {
        return cartService.changeProductQuantity(username, productQuantity);
    }
}
