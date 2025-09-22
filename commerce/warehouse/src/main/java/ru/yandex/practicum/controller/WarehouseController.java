package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

@RestController("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {
    WarehouseService warehouseService;

    @PutMapping
    public ResponseEntity<Void> addNewProduct(NewProductInWarehouseRequest request) {
        return warehouseService.addNewProduct(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductAvailability(shoppingCartDto);
    }


    @PostMapping("/add")
    public ResponseEntity<Void> addProductToWarehouse(AddProductToWarehouseRequest request) {
        return warehouseService.addProductToWarehouse(request);
    }

    @GetMapping("/address")
    public AddressDto getWarehouseAddressForDelivery() {
        return warehouseService.getWarehouseAddressForDelivery();
    }
}
