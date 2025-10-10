package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.ShoppingStoreClient;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.PaymentStatus;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final double FEE_RATE = 0.10; // 10%

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final ShoppingStoreClient shoppingClient;

    @Override
    @Transactional
    public PaymentDto createPayment(OrderDto order) {
        if (order == null || order.getOrderId() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не указан orderId");
        }
        UUID orderId = order.getOrderId();

        OrderDto calculatedOrder = orderClient.calculateTotalCost(orderId);

        double deliveryPrice = nullSafe(calculatedOrder.getDeliveryPrice());
        double productsPrice = nullSafe(calculatedOrder.getProductPrice());
        double orderTotal = nullSafe(calculatedOrder.getTotalPrice(), productsPrice + deliveryPrice);

        double fee = round2(productsPrice * FEE_RATE);
        double totalPayment = round2(orderTotal + fee);

        UUID paymentId = order.getPaymentId() != null ? order.getPaymentId() : UUID.randomUUID();

        // Сохраняем платеж с привязкой к orderId
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .deliveryTotal(deliveryPrice)
                .feeTotal(fee)
                .totalPayment(totalPayment)
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(orderId)
                .build();

        paymentRepository.save(payment);

        return PaymentMapper.toDto(payment);
    }

    @Override
    public double calculateProductCost(OrderDto order) {
        if (order == null || order.getOrderId() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не указан orderId");
        }

        Map<UUID, Long> items = order.getProducts();
        double productsTotal = 0.0;
        if (items != null && !items.isEmpty()) {
            for (Map.Entry<UUID, Long> e : items.entrySet()) {
                ProductDto p = shoppingClient.getProductById(e.getKey());
                productsTotal += p.getPrice() * e.getValue();
            }
        }
        order.setProductPrice(productsTotal);
        Double productPrice = order.getProductPrice();

        return round2(productPrice);
    }

    @Override
    public double calculateTotalCost(OrderDto order) {
        if (order == null || order.getOrderId() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не указан orderId");
        }

        if (order.getDeliveryPrice() == null || order.getDeliveryPrice() == 0.0) {
            throw new NotEnoughInfoInOrderToCalculateException("Не указана стоимость доставки заказа");
        }

        double products = calculateProductCost(order);
        double fee = products * FEE_RATE;
        double delivery = nullSafe(order.getDeliveryPrice());
        double total = products + delivery + fee;

        return round2(total);
    }

    @Override
    @Transactional
    public void refund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж с id=" + paymentId + " не найден"));

        UUID orderId = payment.getOrderId();
        if (orderId == null) {
            // теоретически не должен случаться после правки createPayment
            throw new NotEnoughInfoInOrderToCalculateException(
                    "У платежа " + paymentId + " отсутствует связь с заказом");
        }

        // Уведомляем Order: оплата прошла
        orderClient.payment(orderId);
        log.info("Order {} помечен как оплачен (paymentId={})", orderId, paymentId);

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж с id=" + paymentId + " не найден"));

        UUID orderId = payment.getOrderId();
        if (orderId == null) {
            throw new NotEnoughInfoInOrderToCalculateException(
                    "У платежа " + paymentId + " отсутствует связь с заказом");
        }

        // Уведомляем Order: оплата не прошла
        orderClient.paymentFailed(orderId);
        log.info("Order {} помечен как не оплаченный (paymentId={})", orderId, paymentId);

        payment.setPaymentStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }


    private static double nullSafe(Double v) {
        return Objects.requireNonNullElse(v, 0.0d);
    }

    private static double nullSafe(Double candidate, double fallback) {
        return candidate != null ? candidate : fallback;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0d) / 100.0d;
    }
}
