package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final static String BLANK_NAME = "Имя пользователя не должно быть пустым";
    private final OrderService orderService;

    @GetMapping
    public List<Order> getOrders(@RequestParam @NotBlank(message = BLANK_NAME) String username) {

    }
}
