package ru.yandex.practicum.client;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.OrderDto;

import java.util.UUID;

@FeignClient(name = "order", path = "/api/v1/order", fallback = MyFeignClientFallback.class)
public interface OrderClient {

    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(@RequestBody @NotNull UUID orderId);

    @PostMapping("/payment")
    OrderDto payment(@RequestBody @NotNull UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody @NotNull UUID orderId);

    @GetMapping("/{orderId}")
    OrderDto getOrderById(@RequestBody @NotNull UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody @NotNull UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody @NotNull UUID orderId);

    @PostMapping("/completed")
    OrderDto complete(@RequestBody @NotNull UUID orderId);
}
