package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Warehouse;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    @Query("SELECT w FROM warehouses w WHERE w.address = :address")
    Optional<Warehouse> findByAddress(@Param("address") Address address);
}
