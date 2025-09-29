package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.ProductCartLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCartLinkRepository extends JpaRepository<ProductCartLink, ProductCartLink.ProductCartLinkId> {

    int deleteById_CartIdAndId_ProductId(UUID cartId, UUID productId);

    Optional<ProductCartLink> findById_CartIdAndId_ProductId(UUID cartId, UUID productId);

    List<ProductCartLink> findAllById_CartId(UUID cartId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ProductCartLink l " +
            "SET l.quantity = :quantity " +
            "WHERE l.id.productId = :productId AND l.id.cartId = :cartId")
    int updateQuantity(@Param("cartId") UUID cartId,
                       @Param("productId") UUID productId,
                       @Param("quantity") Long quantity);
}
