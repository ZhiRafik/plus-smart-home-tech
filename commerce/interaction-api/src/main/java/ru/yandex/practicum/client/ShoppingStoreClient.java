package ru.yandex.practicum.client;

import ru.yandex.practicum.dto.ProductDto;

import java.util.UUID;

@FeignClient(name = "shopping-store")
public class ShoppingStoreClient {
    @GetMapping("/api/v1/shopping-store/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId);
}
