package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final static String BLANK_NAME = "Имя пользователя не должно быть пустым";
    private final OrderService orderService;

    @GetMapping
    public List<OrderDto> getOrders(@RequestParam @NotBlank(message = BLANK_NAME) String username) {
        return orderService.getOrders(username);
    }

    @PutMapping
    public OrderDto createNewOrder(@RequestBody @Valid CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    @PostMapping("/payment")
    public OrderDto payment(@RequestBody @NotNull UUID orderId) {
        return orderService.payment(orderId);
    }

    @PostMapping("/payment/failed")
    public OrderDto paymentFailed(@RequestBody @NotNull UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @PostMapping("/delivery")
    public OrderDto delivery(@RequestBody @NotNull UUID orderId) {
        return orderService.delivery(orderId);
    }

    @PostMapping("/delivery/failed")
    public OrderDto deliveryFailed(@RequestBody @NotNull UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @PostMapping("/completed")
    public OrderDto complete(@RequestBody @NotNull UUID orderId) {
        return orderService.complete(orderId);
    }

    @PostMapping("/calculate/total")
    public OrderDto calculateTotalCost(@RequestBody @NotNull UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @PostMapping("/calculate/delivery")
    public OrderDto calculateDeliveryCost(@RequestBody @NotNull UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @PostMapping("/assembly")
    public OrderDto assembly(@RequestBody @NotNull UUID orderId) {
        return orderService.assembly(orderId);
    }

    @PostMapping("/assembly/failed")
    public OrderDto assemblyFailed(@RequestBody @NotNull UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }

    @PostMapping("/return")
    public OrderDto productReturn(@RequestBody @Valid ProductReturnRequest request) {
        return orderService.productReturn(request);
    }
}
