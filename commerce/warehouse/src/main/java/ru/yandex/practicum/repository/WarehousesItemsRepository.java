package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.WarehouseItem;

import java.util.UUID;

public interface WarehousesItemsRepository extends JpaRepository<WarehouseItem, UUID> {
}
