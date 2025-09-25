package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.service.ProductService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController {
    private final ProductService productService;

    @PutMapping
    public ProductDto addProduct(@Valid @RequestBody ProductDto product) {
        return productService.addProduct(product);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto product) {
        return productService.updateProduct(product);
    }

    @GetMapping // manager
    public Page<ProductDto> getProduct(@RequestParam ProductCategory category, Pageable pageable) {
        return productService.getProduct(category, pageable);
    }

    @PostMapping("/removeProductFromStore") // manager
    public ResponseEntity<Boolean> removeProduct(@RequestBody UUID productId) {
        boolean deleted = productService.removeProduct(productId);
        return deleted
                ? ResponseEntity.ok(true)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @PostMapping("/quantityState") // warehouse
    public ResponseEntity<Boolean> setProductState(@RequestParam UUID productId,
                                                   @RequestParam QuantityState quantityState) {
        boolean changed = productService.setProductState(productId, quantityState);
        return changed
                ? ResponseEntity.ok(true)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId);
    }
}