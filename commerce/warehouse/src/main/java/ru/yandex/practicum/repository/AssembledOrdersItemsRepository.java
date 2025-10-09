package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.model.AssembledOrderItem;

import java.util.UUID;

public interface AssembledOrdersItemsRepository extends JpaRepository<AssembledOrderItem, UUID> {

    @Modifying
    @Query("""
       UPDATE AssembledOrderItem aoi SET aoi.deliveryId = :deliveryId
        WHERE aoi.orderId = :orderId
          AND (aoi.deliveryId IS NULL OR aoi.deliveryId = :deliveryId)
    """)
    int assignDeliveryId(UUID orderId, UUID deliveryId);
}
