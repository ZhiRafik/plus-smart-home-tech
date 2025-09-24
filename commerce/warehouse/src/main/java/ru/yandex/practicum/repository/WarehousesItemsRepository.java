package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.model.WarehouseItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehousesItemsRepository extends JpaRepository<WarehouseItem, UUID> {

    List<WarehouseItem> findAllByProductId(UUID productId);

    Optional<WarehouseItem> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

    @Modifying
    @Query("UPDATE WarehouseItem wi SET wi.quantity = wi.quantity + :delta" +
            " WHERE wi.warehouse.id = :warehouseId" +
            " AND wi.productId = :productId")
    int incrementQuantityByWarehouseIdAndProductId(UUID warehouseId, UUID productId, Integer delta);

}
