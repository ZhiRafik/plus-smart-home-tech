package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @PutMapping
    public ResponseEntity<Void> addNewProduct(@RequestBody @Valid NewProductInWarehouseRequest request) {
        return warehouseService.addNewProduct(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductAvailability(@RequestBody @Valid ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductAvailability(shoppingCartDto);
    }


    @PostMapping("/add")
    public ResponseEntity<Void> addProductToWarehouse(@RequestBody @Valid AddProductToWarehouseRequest request) {
        return warehouseService.addProductToWarehouse(request);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddressForDelivery() {
        return warehouseService.getWarehouseAddressForDelivery();
    }
}
