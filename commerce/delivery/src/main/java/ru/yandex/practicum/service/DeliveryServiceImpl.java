package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private static final double BASE_COST = 5.0;
    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto request) {
        if (request == null || request.getOrderId() == null) {
            throw new IllegalArgumentException("Недостаточно данных для создания доставки");
        }

        // Получаем заказ из внешнего сервиса
        OrderDto order = orderClient.getOrderById(request.getOrderId());
        if (order == null) {
            throw new NoDeliveryFoundException("Не найден заказ с id " + request.getOrderId());
        }

        // Строим Delivery на основе заказа и DTO адресов
        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .fromAddress(request.getFromAddress())
                .toAddress(request.getToAddress())
                .deliveryState(DeliveryState.CREATED)
                .totalWeight(order.getDeliveryWeight())   // вес из заказа
                .totalVolume(order.getDeliveryVolume())   // объём из заказа
                .fragile(order.getFragile())              // хрупкость из заказа
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Создана доставка id={} для заказа {}", saved.getDeliveryId(), saved.getOrderId());

        return DeliveryMapper.toDto(saved);
    }


    @Override
    @Transactional
    public void markPicked(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        log.info("Доставка {} переведена в статус IN_PROGRESS", delivery.getDeliveryId());

        // обновляем заказ
        orderClient.delivery(orderId);
        // отправляем информацию о доставке на склад
        warehouseClient.shipToDelivery(
                new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId())
        );
    }

    @Override
    @Transactional
    public void markSuccessful(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        log.info("Доставка {} успешно завершена", delivery.getDeliveryId());

        orderClient.complete(orderId);
    }

    @Override
    @Transactional
    public void markFailed(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);

        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        log.info("Доставка {} завершена с ошибкой", delivery.getDeliveryId());

        orderClient.deliveryFailed(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCost(OrderDto order) {
        Delivery delivery = findByOrderId(order.getOrderId());

        double cost = BASE_COST;

        // 1. множитель по адресу склада
        String fromStreet = delivery.getFromAddress() != null
                ? delivery.getFromAddress().getStreet()
                : "";
        double multiplier = 1.0;
        if (fromStreet.contains("ADDRESS_2")) {
            multiplier = 2.0;
        } else if (fromStreet.contains("ADDRESS_1")) {
            multiplier = 1.0;
        }

        cost = cost * multiplier;
        cost = cost + BASE_COST; // прибавляем базу

        // 2. хрупкость
        if (Boolean.TRUE.equals(delivery.getFragile())) {
            cost += cost * 0.2;
        }

        // 3. вес * 0.3
        cost += safeDouble(delivery.getTotalWeight()) * 0.3;

        // 4. объём * 0.2
        cost += safeDouble(delivery.getTotalVolume()) * 0.2;

        // 5. если улицы не совпадают — добавляем 20%
        String toStreet = delivery.getToAddress() != null
                ? delivery.getToAddress().getStreet()
                : "";

        if (!equalsIgnoreCase(fromStreet, toStreet)) {
            cost += cost * 0.2;
        }

        double finalCost = Math.round(cost * 100.0) / 100.0;
        log.info("Стоимость доставки для заказа {} рассчитана: {}", order.getOrderId(), finalCost);

        return finalCost;
    }

    private Delivery findByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка для заказа " + orderId + " не найдена"));
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return Objects.toString(a, "").equalsIgnoreCase(Objects.toString(b, ""));
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
