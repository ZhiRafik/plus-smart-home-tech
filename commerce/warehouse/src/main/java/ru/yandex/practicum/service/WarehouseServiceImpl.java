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
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.AddressDtoMapper;
import ru.yandex.practicum.mapper.DimensionMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.Warehouse;
import ru.yandex.practicum.model.WarehouseItem;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.repository.WarehousesItemsRepository;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehousesItemsRepository warehousesItemsRepository;
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

    public AddressDto getWarehouseAddressForDelivery() {
        return AddressDtoMapper.mapToDto(
                warehouseRepository.findById(currentWarehouseId).get().getAddress());
    }

    private Double calculateVolume(Dimension dimension) {
        return dimension.getDepth() * dimension.getHeight() * dimension.getWidth();
    }
}
