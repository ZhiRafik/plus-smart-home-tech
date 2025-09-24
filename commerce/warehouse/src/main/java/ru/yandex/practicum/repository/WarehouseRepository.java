package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Warehouse;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    @Query("""
       SELECT w FROM Warehouse w
       WHERE w.address.country = :#{#address.country}
         AND w.address.city = :#{#address.city}
         AND w.address.street = :#{#address.street}
         AND w.address.house = :#{#address.house}
         AND w.address.flat = :#{#address.flat}
       """)
    Optional<Warehouse> findByAddress(@Param("address") Address address);
}
