package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody OrderDto order) {
        PaymentDto payment = paymentService.createPayment(order);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/productCost")
    public ResponseEntity<Double> calculateProductCost(@RequestBody OrderDto order) {
        double cost = paymentService.calculateProductCost(order);
        return ResponseEntity.ok(cost);
    }

    @PostMapping("/totalCost")
    public ResponseEntity<Double> calculateTotalCost(@RequestBody OrderDto order) {
        double total = paymentService.calculateTotalCost(order);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/refund")
    public ResponseEntity<Void> refund(@RequestBody @NotNull UUID paymentId) {
        paymentService.refund(paymentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/failed")
    public ResponseEntity<Void> paymentFailed(@RequestBody @NotNull UUID paymentId) {
        paymentService.paymentFailed(paymentId);
        return ResponseEntity.ok().build();
    }
}
