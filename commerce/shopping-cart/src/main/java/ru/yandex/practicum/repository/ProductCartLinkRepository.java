package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ProductCartLink;

import java.util.UUID;

public interface ProductCartLinkRepository extends JpaRepository<ProductCartLink, ProductCartLink.ProductCartLinkId> {
    int deleteByCartIdAndProductId(UUID cartId, UUID productId);

    @Query("UPDATE ProductCartLink l " +
            "SET l.quantity = :quantity " +
            "WHERE l.id.productId = :productId AND l.id.cartId = :cartId")
    int updateQuantity(@Param("cartId") UUID cartId,
                       @Param("productId") UUID productId,
                       @Param("quantity") Long quantity);
}
