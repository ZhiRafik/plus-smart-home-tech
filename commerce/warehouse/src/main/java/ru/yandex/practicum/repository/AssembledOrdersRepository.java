package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.AssembledOrder;

import java.util.Optional;
import java.util.UUID;

public interface AssembledOrdersRepository extends JpaRepository<AssembledOrder, UUID> {

    Optional<AssembledOrder> findByOrderId(UUID orderId);
}
