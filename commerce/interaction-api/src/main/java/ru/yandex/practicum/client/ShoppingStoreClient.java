package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.request.SetQuantityRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
interface ShoppingStoreClient {

    @PutMapping
    ProductDto addProduct(@RequestBody ProductDto product);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto product);

    @GetMapping
    Page<ProductDto> getProduct(@RequestParam ProductCategory category, Pageable pageable);

    @PostMapping("/removeProductFromStore") // manager
    ResponseEntity<Boolean> removeProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState") // warehouse
    ResponseEntity<Boolean> setProductState(@RequestBody SetQuantityRequest request);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);
}
