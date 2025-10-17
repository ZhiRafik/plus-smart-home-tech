package ru.yandex.practicum.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NoOrderFoundInWarehouseException;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.AddressDtoMapper;
import ru.yandex.practicum.mapper.DimensionMapper;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.AssembledOrdersItemsRepository;
import ru.yandex.practicum.repository.AssembledOrdersRepository;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.repository.WarehousesItemsRepository;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehousesItemsRepository warehousesItemsRepository;
    private final AssembledOrdersRepository assembledOrdersRepository;
    private final AssembledOrdersItemsRepository assembledOrdersItemsRepository;
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS
            = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];
    private static UUID currentWarehouseId;

    @PostConstruct
    @Transactional
    public void initWarehouse() {
        Address address = Address.of(CURRENT_ADDRESS);

        Optional<Warehouse> existing = warehouseRepository.findByAddress(address);
        if (existing.isPresent()) {
            currentWarehouseId = existing.get().getId();
            return;
        }

        try {
            currentWarehouseId = warehouseRepository.save(Warehouse.builder()
                    .address(address)
                    .build())
                    .getId();
        } catch (DataIntegrityViolationException dup) {
            // кто-то уже создал параллельно — просто читаем
            currentWarehouseId = warehouseRepository.findByAddress(address).orElseThrow().getId();
        }
    }

    // на моменте любого метода у нас уже PostConstruct сохранил в БД какой-то Warehouse
    @Transactional
    public ResponseEntity<Void> addNewProduct(NewProductInWarehouseRequest request) {
        if (warehousesItemsRepository.findByWarehouseIdAndProductId(
                currentWarehouseId, request.getProductId())
                .isPresent()) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    String.format("Product with ID %s already exists in the warehouse with ID %s",
                            request.getProductId(), currentWarehouseId)
            );
        }
        WarehouseItem item = WarehouseItem.builder()
                .warehouse(warehouseRepository.findById(currentWarehouseId).get()) // инициализирован в PostConstruct
                .productId(request.getProductId())                                         // => точно есть
                .dimension(DimensionMapper.mapToDimension(request.getDimension()))
                .fragile(request.getFragile())
                .weight(request.getWeight())
                .quantity(0L)
                .build();
        warehousesItemsRepository.save(item);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public BookedProductsDto checkProductAvailability(ShoppingCartDto shoppingCartDto) {
        Double deliveryWeight = 0.0;
        Double deliveryVolume = 0.0;
        Boolean fragile = false;

        for (Map.Entry<UUID, Long> e : shoppingCartDto.getProducts().entrySet()) {
            UUID productId = e.getKey();
            Long quantity = e.getValue();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0");
            }

            Optional<WarehouseItem> optionalItem = warehousesItemsRepository
                    .findByWarehouseIdAndProductId(currentWarehouseId, productId);
            if (optionalItem.isEmpty()) {
                throw new NoSpecifiedProductInWarehouseException(
                        String.format("Product with ID %s was not found in the warehouse with ID %s",
                                productId, currentWarehouseId)
                );
            } else {
                if (optionalItem.get().getQuantity() < quantity) {
                    throw new ProductInShoppingCartLowQuantityInWarehouseException(
                            String.format("Not enough product with ID %s in the warehouse with ID %s. " +
                                            "Needed: %d, found: %d",
                                    productId, currentWarehouseId, quantity, optionalItem.get().getQuantity())
                    );
                }
                if (optionalItem.get().getFragile()) {
                    fragile = true;
                }
                deliveryWeight += optionalItem.get().getWeight();
                deliveryVolume += calculateVolume(optionalItem.get().getDimension());
            }
        };

        return BookedProductsDto.builder()
                .deliveryVolume(deliveryVolume)
                .deliveryWeight(deliveryWeight)
                .fragile(fragile)
                .build();
    }

    @Transactional
    public ResponseEntity<Void> addProductToWarehouse(AddProductToWarehouseRequest request) {
        int changed = warehousesItemsRepository.incrementQuantityByWarehouseIdAndProductId(
                currentWarehouseId, request.getProductId(), request.getQuantity());
        if (changed == 0) {
            throw new NoSpecifiedProductInWarehouseException(
                    String.format("Product with ID %s was not found in the warehouse with ID %s",
                            request.getProductId(), currentWarehouseId)
            );
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        Double deliveryWeight = 0.0;
        Double deliveryVolume = 0.0;
        Boolean fragile = false;

        UUID orderId = request.getOrderId();
        if (orderId == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("orderId and products must be provided");
        }

        assembledOrdersRepository.findByOrderId(orderId)
                .orElseGet(() -> assembledOrdersRepository.save(
                        AssembledOrder.builder().orderId(orderId).deliveryId(null).build()
                ));

        List<AssembledOrderItem> itemsToSave = new ArrayList<>(request.getProducts().size());

        for (Map.Entry<UUID, Long> e : request.getProducts().entrySet()) {
            UUID productId = e.getKey();
            Long quantity = e.getValue();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0");
            }

            Optional<WarehouseItem> optionalItem = warehousesItemsRepository
                    .findByWarehouseIdAndProductId(currentWarehouseId, productId);
            if (optionalItem.isEmpty()) {
                throw new NoSpecifiedProductInWarehouseException(
                        String.format("Product with ID %s was not found in the warehouse with ID %s",
                                productId, currentWarehouseId)
                );
            } else {
                if (optionalItem.get().getQuantity() < quantity) {
                    throw new ProductInShoppingCartLowQuantityInWarehouseException(
                            String.format("Not enough product with ID %s in the warehouse with ID %s. " +
                                            "Needed: %d, found: %d",
                                    productId, currentWarehouseId, quantity, optionalItem.get().getQuantity())
                    );
                }
                if (optionalItem.get().getFragile()) {
                    fragile = true;
                }

                Long currQuantity = optionalItem.get().getQuantity();
                Long newQuantity = currQuantity - quantity;
                warehousesItemsRepository.setQuantity(currentWarehouseId, productId, newQuantity);

                itemsToSave.add(AssembledOrderItem.builder()
                        .orderId(orderId)
                        .deliveryId(null)
                        .productId(productId)
                        .quantity(quantity)
                        .build());

                deliveryWeight += optionalItem.get().getWeight();
                deliveryVolume += calculateVolume(optionalItem.get().getDimension());
            }
        } // здесь мы ещё не знаем deliveryId и сохраняем бронь БЕЗ deliveryId - это поле в БД == null

        assembledOrdersItemsRepository.saveAll(itemsToSave);

        return BookedProductsDto.builder()
                .deliveryVolume(deliveryVolume)
                .deliveryWeight(deliveryWeight)
                .fragile(fragile)
                .build();
    }

    @Transactional
    public void returnProducts(Map<UUID, Long> products) {
        for (Map.Entry<UUID, Long> e : products.entrySet()) {
            UUID productId = e.getKey();
            Long quantity = e.getValue();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0");
            }

            int updated = warehousesItemsRepository.incrementQuantity(currentWarehouseId, productId, quantity);
            if (updated == 0) {
                throw new NoSpecifiedProductInWarehouseException(
                        "Product %s not found in warehouse %s".formatted(productId, currentWarehouseId));
            }
        } // метод никак не взаимодействует БД, по API информация о заказах НЕ обновляется
    }

    @Transactional // вот здесь мы уже получаем deliveryId и добавляем его в БД, т.к. инициализируется доставка
    public void shipToDelivery(ShippedToDeliveryRequest request) {
        UUID orderId = request.getOrderId();
        UUID deliveryId = request.getDeliveryId();

        if (orderId == null || deliveryId == null) {
            throw new IllegalArgumentException("orderId and deliveryId must be provided");
        }

        AssembledOrder assembledOrder = assembledOrdersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundInWarehouseException(
                        "Assembled order %s not found in warehouse".formatted(orderId)));

        UUID existing = assembledOrder.getDeliveryId();
        if (existing != null && !existing.equals(deliveryId)) {
            throw new IllegalStateException(
                    "Order %s is already linked to delivery %s".formatted(orderId, existing));
        }

        if (existing == null) {
            assembledOrder.setDeliveryId(deliveryId);
            assembledOrdersRepository.save(assembledOrder);
        }

        int updatedRows = assembledOrdersItemsRepository.assignDeliveryId(orderId, deliveryId);

        if (updatedRows == 0) {
            throw new IllegalStateException(
                    "No assembled items found for order " + orderId + " to assign deliveryId");
        }
    }

    public AddressDto getWarehouseAddressForDelivery() {

        return AddressDtoMapper.mapToDto(
                warehouseRepository.findById(currentWarehouseId).
                        orElseThrow(() -> new IllegalStateException("No warehouses were added"))
                        .getAddress()
        );
    }

    private Double calculateVolume(Dimension dimension) {
        return dimension.getDepth() * dimension.getHeight() * dimension.getWidth();
    }
}
