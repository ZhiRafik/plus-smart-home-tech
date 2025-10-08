package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exception.InvalidOrderStateException;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.*;
import ru.yandex.practicum.request.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final ShoppingClient shoppingClient;

    @Override
    public List<OrderDto> getOrders(String username) {
        List<Order> orders = orderRepository.findAllByUsername(username);
        if (orders.isEmpty()) {
            throw new NoOrderFoundException(
                    String.format("No orders for user %s were found", username)
            );
        }
        return orders.stream()
                     .map(OrderMapper::toDto)
                     .toList();
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new NotAuthorizedUserException("User is not authenticated"));


        ShoppingCartDto cart = request.getShoppingCart();
        BookedProductsDto booked = warehouseClient.checkProductAvailability(cart);

        DeliveryCreateResponse delivery = deliveryClient.create(
                DeliveryCreateRequest.builder()
                        .toAddress(request.getDeliveryAddress())
                        .weight(booked.getDeliveryWeight())
                        .volume(booked.getDeliveryVolume())
                        .fragile(Boolean.TRUE.equals(booked.getFragile()))
                        .build()
        );

        UUID deliveryId = delivery.getDeliveryId();
        Double deliveryPrice = delivery.getPrice();

        PaymentCreateRequest paymentRequest = PaymentCreateRequest.builder()
                .items(cart.getProducts())
                .deliveryCost(deliveryPrice)
                .vatPercent(10.0)
                .build();

        PaymentCreateResponse paymentCreated = paymentClient.create(paymentRequest);
        UUID paymentId = paymentCreated.getPaymentId();

        Order order = Order.builder()
                .state(OrderState.NEW)
                .username(username)
                .shoppingCartId(cart.getCartId())
                .deliveryAddress(request.getDeliveryAddress())
                .products(cart.getProducts())
                .paymentId(paymentId)
                .deliveryId(deliveryId)
                .deliveryPrice(deliveryPrice)
                .build();

        Order saved = orderRepository.save(order);
        return OrderMapper.toDto(saved);
    }


    @Override
    public OrderDto payment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        // идемпотентность: если уже оплачен — возвращаем как есть
        if (order.getState() == OrderState.PAID) {
            return OrderMapper.toDto(order);
        }

        // допустимые исходные состояния — до сборки
        if (!EnumSet.of(OrderState.NEW, OrderState.ON_PAYMENT).contains(order.getState())) {
            throw new InvalidOrderStateException("Cannot mark paid from state %s".formatted(order.getState()));
        }

        order.setState(OrderState.PAID);
        orderRepository.save(order);
        return OrderMapper.toDto(order);
    }


    @Override
    public OrderDto paymentFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(
                        String.format("No order with %s was found", orderId)
                ));

        // идемпотентность
        if (order.getState() == OrderState.PAYMENT_FAILED) {
            return OrderMapper.toDto(order);
        }

        // проверка допустимого перехода
        if (!EnumSet.of(OrderState.NEW, OrderState.ON_PAYMENT)
                .contains(order.getState())) {
            throw new InvalidOrderStateException(
                    String.format("Cannot mark payment failed from state %s", order.getState())
            );
        }

        // фиксация статуса
        order.setState(OrderState.PAYMENT_FAILED);
        orderRepository.save(order);

        // компенсации (best-effort)
        try {
            if (order.getDeliveryId() != null) {
                deliveryClient.cancel(order.getDeliveryId());
            }
        } catch (Exception e) {
            log.warn("Failed to cancel delivery for order {}", orderId, e);
        }

        try {
            warehouseClient.returnToStock(orderId);
        } catch (Exception e) {
            log.warn("Failed to return stock for order {}", orderId, e);
        }

        return OrderMapper.toDto(order);
    }


    @Override
    public OrderDto delivery(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        if (order.getState() == OrderState.ON_DELIVERY) {
            return OrderMapper.toDto(order);
        }

        if (!EnumSet.of(OrderState.ASSEMBLED).contains(order.getState())) {
            throw new InvalidOrderStateException("Cannot move to ON_DELIVERY from state %s".formatted(order.getState()));
        }
        if (order.getDeliveryId() == null) {
            throw new InvalidOrderStateException("Cannot start delivery: deliveryId is null");
        }

        order.setState(OrderState.ON_DELIVERY);
        orderRepository.save(order);
        return OrderMapper.toDto(order);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        if (order.getState() == OrderState.DELIVERY_FAILED) {
            return OrderMapper.toDto(order);
        }

        if (!EnumSet.of(OrderState.ASSEMBLED, OrderState.ON_DELIVERY).contains(order.getState())) {
            throw new InvalidOrderStateException("Cannot mark delivery failed from state %s".formatted(order.getState()));
        }

        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);

        try {
            warehouseClient.returnToStock(orderId);
        } catch (Exception e) {
            log.warn("Failed to return stock for order {}", orderId, e);
        }

        return OrderMapper.toDto(order);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        if (EnumSet.of(OrderState.COMPLETED).contains(order.getState())) {
            return OrderMapper.toDto(order);
        }

        if (order.getState() != OrderState.DELIVERED) {
            throw new InvalidOrderStateException("Cannot complete order from state %s".formatted(order.getState()));
        }

        order.setState(OrderState.COMPLETED);
        orderRepository.save(order);

        return OrderMapper.toDto(order);
    }


    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        Map<UUID, Long> items = order.getProducts();
        double productsTotal = 0.0;
        if (items != null && !items.isEmpty()) {
            for (Map.Entry<UUID, Long> e : items.entrySet()) {
                ProductDto p = shoppingClient.getProductById(e.getKey());
                productsTotal += p.getPrice() * e.getValue();
            }
        }
        order.setProductPrice(productsTotal);

        double deliveryPrice = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : 0.0;
        order.setTotalPrice(productsTotal + deliveryPrice);

        orderRepository.save(order);
        return OrderMapper.toDto(order);
    }



    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        // Ничего не пересчитываем: цена доставки уже задана при создании заказа.
        // Гарантируем ненулевое значение в ответе.
        if (order.getDeliveryPrice() == null) {
            order.setDeliveryPrice(0.0);
            orderRepository.save(order);
        }

        return OrderMapper.toDto(order);
    }


    @Override
    public OrderDto assembly(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        if (order.getState() == OrderState.ASSEMBLED) {
            return OrderMapper.toDto(order);
        }

        if (!EnumSet.of(OrderState.PAID).contains(order.getState())) {
            throw new InvalidOrderStateException("Cannot move to ASSEMBLED from state %s".formatted(order.getState()));
        }

        ShoppingCartDto cart = ShoppingCartDto.builder()
                .cartId(order.getShoppingCartId())
                .products(order.getProducts())
                .build();

        warehouseClient.checkProductAvailability(cart);
        warehouseClient.assemble(orderId, cart); // вычитаем остатки и помечаем как собранные на складе
        order.setState(OrderState.ASSEMBLED);
        orderRepository.save(order);

        return OrderMapper.toDto(order);
    }



    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        if (order.getState() == OrderState.ASSEMBLY_FAILED) {
            return OrderMapper.toDto(order);
        }

        if (!EnumSet.of(OrderState.PAID, OrderState.ON_PAYMENT, OrderState.ASSEMBLED).contains(order.getState())) {
            throw new InvalidOrderStateException("Cannot mark assembly failed from state %s".formatted(order.getState()));
        }

        try {
            warehouseClient.returnToStock(orderId); // вернуть на склад, т.к. сборка не состоялась
        } catch (Exception e) {
            log.warn("Failed to return stock for order {}", orderId, e);
        }

        order.setState(OrderState.ASSEMBLY_FAILED);
        orderRepository.save(order);

        return OrderMapper.toDto(order);
    }


    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        UUID orderId = request.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("No order with %s was found".formatted(orderId)));

        if (order.getState() == OrderState.PRODUCT_RETURNED) {
            return OrderMapper.toDto(order);
        }

        if (!EnumSet.of(OrderState.DELIVERED, OrderState.DONE, OrderState.COMPLETED)
                .contains(order.getState())) {
            throw new InvalidOrderStateException(
                    "Cannot return products for order in state %s".formatted(order.getState()));
        }

        try {
            warehouseClient.returnProducts(request);
        } catch (Exception e) {
            log.warn("Failed to return products for order {}", orderId, e);
        }

        order.setState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);
        return OrderMapper.toDto(order);
    }
}
