package ru.yandex.practicum.client;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.SetQuantityRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MyFeignClientFallback implements WarehouseClient, PaymentClient,
                                              DeliveryClient, ShoppingCartClient,
                                              ShoppingStoreClient {

    @Override
    public BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto) {
        throw new RuntimeException("Сервис Warehouse временно не доступен");
    }

    @Override
    public AddressDto getWarehouseAddressForDelivery() {
        throw new RuntimeException("Сервис Warehouse временно не доступен");
    }

    @PostMapping("/shipped")
    public ResponseEntity<Void> shipToDelivery(@RequestBody ShippedToDeliveryRequest request) {
        throw new RuntimeException("Сервис Warehouse временно не доступен");
    }

    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductsInOrder(@RequestBody AssemblyProductsForOrderRequest request) {
        throw new RuntimeException("Сервис Warehouse временно не доступен");
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody OrderDto order) {
        throw new RuntimeException("Сервис Payment временно не доступен");
    }

    @PostMapping("/totalCost")
    public ResponseEntity<Double> calculateTotalCost(@RequestBody OrderDto order) {
        throw new RuntimeException("Сервис Payment временно не доступен");
    }

    @PutMapping
    public DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto dto) {
        throw new RuntimeException("Сервис Delivery временно не доступен");
    }

    @PostMapping("/cost")
    public Double deliveryCost(@RequestBody @Valid OrderDto order) {
        throw new RuntimeException("Сервис Delivery временно не доступен");
    }

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam String username, @RequestBody Map<UUID, Long> products) {
        throw new RuntimeException("Сервис ShoppingCart временно не доступен");
    }

    @GetMapping
    public ShoppingCartDto getCart(@RequestParam String username) {
        throw new RuntimeException("Сервис ShoppingCart временно не доступен");
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivateCart(@RequestParam String username) {
        throw new RuntimeException("Сервис ShoppingCart временно не доступен");
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(@RequestParam String username, @RequestBody List<UUID> products) {
        throw new RuntimeException("Сервис ShoppingCart временно не доступен");
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam String username,
                                                 @RequestBody Map<UUID, Long> productsQuantity) {
        throw new RuntimeException("Сервис ShoppingCart временно не доступен");
    }

    @PutMapping
    public ProductDto addProduct(@RequestBody ProductDto product) {
        throw new RuntimeException("Сервис ShoppingStore временно не доступен");
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto product) {
        throw new RuntimeException("Сервис ShoppingStore временно не доступен");
    }

    @GetMapping
    public Page<ProductDto> getProduct(@RequestParam ProductCategory category, Pageable pageable) {
        throw new RuntimeException("Сервис ShoppingStore временно не доступен");
    }

    @PostMapping("/removeProductFromStore") // manager
    public ResponseEntity<Boolean> removeProduct(@RequestBody UUID productId) {
        throw new RuntimeException("Сервис ShoppingStore временно не доступен");
    }

    @PostMapping("/quantityState") // warehouse
    public ResponseEntity<Boolean> setProductState(@RequestBody SetQuantityRequest request) {
        throw new RuntimeException("Сервис ShoppingStore временно не доступен");
    }

    @GetMapping("/{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {
        throw new RuntimeException("Сервис ShoppingStore временно не доступен");
    }

}
