package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.ProductReturnLink;

import java.util.UUID;

public interface ProductReturnsRepository extends JpaRepository<ProductReturnLink, UUID> {
}
