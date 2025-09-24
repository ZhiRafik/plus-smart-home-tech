package ru.yandex.practicum.client;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;

@Component
public class MyFeignClientFallback implements WarehouseClient {

    @Override
    public BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto) {
        throw new RuntimeException("Сервис Warehouse временно не доступен");
    }

    @Override
    public AddressDto getWarehouseAddressForDelivery() {
        throw new RuntimeException("Сервис Warehouse временно не доступен");
    }
}
