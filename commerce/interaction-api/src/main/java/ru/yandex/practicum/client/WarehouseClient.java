package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse", fallback = MyFeignClientFallback.class)
public interface WarehouseClient {

    @PostMapping("/check")
    BookedProductsDto checkProductAvailability(@RequestBody ShoppingCartDto shoppingCartDto);

    @GetMapping("/address")
    AddressDto getWarehouseAddressForDelivery();

    @PostMapping("/shipped")
    ResponseEntity<Void> shipToDelivery(@RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/assembly")
    BookedProductsDto assemblyProductsInOrder(@RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/return")
    ResponseEntity<Void> returnProducts(@RequestBody Map<UUID, Long> products);
}
