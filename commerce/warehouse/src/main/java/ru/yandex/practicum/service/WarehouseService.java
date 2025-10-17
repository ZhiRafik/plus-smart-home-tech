package ru.yandex.practicum.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    ResponseEntity<Void> addNewProduct(NewProductInWarehouseRequest request);

    BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto);

    ResponseEntity<Void> addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddressForDelivery();

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);

    void returnProducts(Map<UUID, Long> products);

    void shipToDelivery(ShippedToDeliveryRequest request);
}
