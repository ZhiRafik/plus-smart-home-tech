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
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.model.SetQuantityRequest;
import ru.yandex.practicum.service.ProductService;

import java.util.UUID;

@RestController("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PutMapping
    public ProductDto addProduct(@Valid @RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody Product product) {
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
    public ResponseEntity<Boolean> setProductState(@RequestBody SetQuantityRequest request) {
        boolean changed = productService.setProductState(request.getProductId(), request.getQuantityState());
        return changed
                ? ResponseEntity.ok(true)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId);
    }
}