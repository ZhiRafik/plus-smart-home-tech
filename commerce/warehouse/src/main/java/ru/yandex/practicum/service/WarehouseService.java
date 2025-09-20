package ru.yandex.practicum.service;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.model.AddProductToWarehouseRequest;
import ru.yandex.practicum.model.NewProductInWarehouseRequest;

public interface WarehouseService {

    ResponseEntity<Void> addNewProduct(NewProductInWarehouseRequest request);

    BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto);

    ResponseEntity<Void> addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddressForDelivery();
}
