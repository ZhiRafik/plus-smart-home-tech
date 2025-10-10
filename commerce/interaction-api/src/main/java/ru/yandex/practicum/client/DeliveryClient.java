package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;

@FeignClient(name = "delivery", path = "/api/v1/delivery", fallback = MyFeignClientFallback.class)
public interface DeliveryClient {

    @PutMapping
    DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto dto);

    @PostMapping("/cost")
    Double deliveryCost(@RequestBody @Valid OrderDto order);
}
