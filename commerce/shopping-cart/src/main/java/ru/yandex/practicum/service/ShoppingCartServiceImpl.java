package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.WarehouseClient;
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
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final WarehouseClient warehouseClient;
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
        ShoppingCart foundCart;

        if (cart.isEmpty()) {
            foundCart = ShoppingCart.builder()
                    .isActive(true)
                    .username(username)
                    .products(products)
                    .build();
        } else { // иначе корзина уже есть, можно только добавить связки продукт-корзина в БД cart_products
            // сначала вернём текущие товары, чтобы добавить их к новым и вернуть DTO
            foundCart = cart.get();
            Map<UUID, Long> unitedProducts = foundCart.getProducts();
            unitedProducts.putAll(products);
            foundCart.setProducts(unitedProducts);
        }

        // нужно сразу проверить наличие каждого товара на складе и в противном случае ничего не добавлять
        warehouseClient.checkProductAvailability(ShoppingCartDto.builder()
                        .cartId(foundCart.getId())
                        .products(products)
                        .build());
        // пока ничего не делаю с возвращаемым BookedProductsDto,
        // но в случае нехватки выкинется ProductInShoppingCartLowQuantityInWarehouseException

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();
            productCartLinkRepository.save(ProductCartLink.builder()
                    .cart(foundCart)
                    .id(ProductCartLink.ProductCartLinkId.builder()
                            .productId(productId)
                            .cartId(foundCart.getId())
                            .build())
                    .quantity(quantity)
                    .build());
        }

        return ShoppingCartMapper.mapCartToDto(foundCart);
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
                .getId();

        for (UUID productId : products) {
            if (productCartLinkRepository.deleteById_CartIdAndId_ProductId(cartId, productId) < 1) {
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
                .getId();

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