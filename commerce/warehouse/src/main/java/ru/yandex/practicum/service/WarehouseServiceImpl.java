package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.repository.WarehousesItemsRepository;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehousesItemsRepository warehousesItemsRepository;
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS
            = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];

    public ResponseEntity<Void> addNewProduct(NewProductInWarehouseRequest request) {

    }

    public BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto) {

    }

    public ResponseEntity<Void> addProductToWarehouse(AddProductToWarehouseRequest request) {

    }

    public AddressDto getWarehouseAddressForDelivery() {

    }
}
