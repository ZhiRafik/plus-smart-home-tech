package ru.yandex.practicum.service;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NoSuchProductInShoppingCartException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ProductCartLink;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ProductCartLinkRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductCartLinkRepository productCartLinkRepository;
    private final static String NO_PRODUCTS_IN_CART = "No products in cart yet";
    private final static String NO_SUCH_PRODUCT_IN_CART = "Product with ID %s not found in the cart %s";

    public ShoppingCartDto addProduct(String username, Map<UUID, Long> products) {
        // 1. Есть ли такая корзина? Если да, добавим туда, иначе создадим новую по имени пользователя
        // 2. Активна ли эта корзина? Если нет, то стоп
        // 3. По найденной корзине по имени пользователя добавляем товар и его количество в связную таблицу
        // 4. Проверить наличие на складе
        Optional<ShoppingCart> cart = cartRepository.findByUsername(username);

        if (cart.isEmpty()) {
            return ShoppingCartMapper.mapCartToDto(
                    ShoppingCart.builder()
                    .isActive(true)
                    .username(username)
                    .products(products)
                    .build()
            );
        } else { // иначе корзина уже есть, можно только добавить связки продукт-корзина в БД cart_products
            // сначала вернём текущие товары, чтобы добавить их к новым и вернуть DTO
            ShoppingCart foundCart = cart.get();

            for (Map.Entry<UUID, Long> entry : products.entrySet()) {
                UUID productId = entry.getKey();
                Long quantity = entry.getValue();
                productCartLinkRepository.save(ProductCartLink.builder()
                        .cart(foundCart)
                        .id(ProductCartLink.ProductCartLinkId.builder()
                                .productId(productId)
                                .cartId(foundCart.getCartId())
                                .build())
                        .quantity(quantity)
                        .build());
            }

            Map<UUID, Long> unitedProducts = foundCart.getProducts();
            unitedProducts.putAll(products);
            foundCart.setProducts(unitedProducts);

            return ShoppingCartMapper.mapCartToDto(foundCart);
        }
    }

    public ShoppingCartDto getCart(String username) { // валидация имени пользователя в контроллере
        return ShoppingCartMapper.mapCartToDto(cartRepository.findByUsername(username).
                orElseThrow(() -> new NoProductsInShoppingCartException(NO_PRODUCTS_IN_CART)));
    }

    public ResponseEntity<Void> deactivateCart(String username) {
        int changed = cartRepository.deactivate(username);
        if (changed > 0) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ShoppingCartDto removeProducts(String username, List<UUID> products) {
        UUID cartId = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NoProductsInShoppingCartException(NO_PRODUCTS_IN_CART))
                .getCartId();

        for (UUID productId : products) {
            if (productCartLinkRepository.deleteByCartIdAndProductId(cartId, productId) < 1) {
                throw new NoSuchProductInShoppingCartException(
                        String.format(NO_SUCH_PRODUCT_IN_CART, productId));
            }
        }
        // выше уже проверили, что корзина существует
        return ShoppingCartMapper.mapCartToDto(
                cartRepository.findById(cartId).get());
    }

    public ShoppingCartDto changeProductQuantity(String username,
                                                 Map<UUID, Long> productsQuantity) {
        UUID cartId = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NoProductsInShoppingCartException(NO_PRODUCTS_IN_CART))
                .getCartId();

        for (Map.Entry<UUID, Long> entry : productsQuantity.entrySet()) {
            if (productCartLinkRepository.updateQuantity(cartId, entry.getKey(), entry.getValue()) < 1) {
                throw new NoSuchProductInShoppingCartException(
                        String.format(NO_SUCH_PRODUCT_IN_CART, entry.getKey(), cartId));
            }
        }
        // выше уже проверили, что корзина существует
        return ShoppingCartMapper.mapCartToDto(
                cartRepository.findById(cartId).get());
    }
}