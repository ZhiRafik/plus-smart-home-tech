package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<ShoppingCart, UUID> {
    Optional<ShoppingCart> findByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE ShoppingCart c
              SET c.isActive = false
            WHERE c.username = :username
              AND c.isActive = true
           """)
    int deactivate(@Param("username") String username);
}
