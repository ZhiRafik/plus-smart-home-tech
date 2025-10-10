package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.ProductReturnItemLink;

import java.util.UUID;

public interface ProductReturnItemsRepository extends JpaRepository<ProductReturnItemLink, UUID> {
}
