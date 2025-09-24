package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "api/v1/shopping-cart")
public interface ShoppingCartClient {

    @PutMapping
    ShoppingCartDto addProduct(@RequestParam String username, @RequestBody Map<UUID, Long> products);

    @GetMapping
    ShoppingCartDto getCart(@RequestParam String username);

    @DeleteMapping
    ResponseEntity<Void> deactivateCart(@RequestParam String username);

    @PostMapping("/remove")
    ShoppingCartDto removeProducts(@RequestParam String username, @RequestBody List<UUID> products);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam String username, @RequestBody Map<UUID, Long> productsQuantity);
}
