package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

@FeignClient(name = "payment", path = "/api/v1/payment", fallback = MyFeignClientFallback.class)
public interface PaymentClient {

    @PostMapping
    ResponseEntity<PaymentDto> createPayment(@RequestBody OrderDto order);

    @PostMapping("/totalCost")
    ResponseEntity<Double> calculateTotalCost(@RequestBody OrderDto order);
}
