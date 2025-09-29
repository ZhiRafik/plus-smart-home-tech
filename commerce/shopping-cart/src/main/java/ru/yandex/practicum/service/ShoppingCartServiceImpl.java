package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.WarehouseClient;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NoSuchProductInShoppingCartException;
import ru.yandex.practicum.model.ProductCartLink;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ProductCartLinkRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final WarehouseClient warehouseClient;
    private final CartRepository cartRepository;
    private final ProductCartLinkRepository productCartLinkRepository;
    private final static String NO_PRODUCTS_IN_CART = "No products in cart yet";
    private final static String NO_SUCH_PRODUCT_IN_CART = "Product with ID %s not found in the cart %s";

    @Transactional
    public ShoppingCartDto addProduct(String username, Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            log.warn("products_is_empty");
            throw new IllegalArgumentException("Products map is empty");
        }
        log.debug("start_add_product username={} products_count={} products={}", username, products.size(), products);
        // 1. Есть ли такая корзина? Если да, добавим туда, иначе создадим новую по имени пользователя
        // 2. Активна ли эта корзина? Если нет, то стоп
        // 3. По найденной корзине по имени пользователя добавляем товар и его количество в связную таблицу
        // 4. Проверить наличие на складе
        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    ShoppingCart created = cartRepository.saveAndFlush(
                            ShoppingCart.builder()
                                    .isActive(true)
                                    .username(username)
                                    .build()
                    );
                    log.debug("cart_created with cartId={}", created.getId());
                    return created;
                });

        if (Boolean.FALSE.equals(cart.getIsActive())) {
            log.warn("cart_inactive cartId={}", cart.getId());
            throw new IllegalStateException("Cart is not active");
        }
        log.info("cart_ready cartId={}", cart.getId());

        log.debug("warehouse_check_request cartId={} products={}", cart.getId(), products);
        // нужно сразу проверить наличие каждого товара на складе и в противном случае ничего не добавлять
        warehouseClient.checkProductAvailability(ShoppingCartDto.builder()
                        .cartId(cart.getId())
                        .products(products)
                        .build()
        );
        // пока ничего не делаю с возвращаемым BookedProductsDto,
        // но в случае нехватки выкинется ProductInShoppingCartLowQuantityInWarehouseException

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long addQuantity = entry.getValue();

            log.debug("upsert_link_find cartId={} productId={}", cart.getId(), productId);
            ProductCartLink link = productCartLinkRepository
                    .findById_CartIdAndId_ProductId(cart.getId(), productId)
                    .orElseGet(() -> {
                        ProductCartLink created = ProductCartLink.builder()
                            .cart(cart)
                            .id(ProductCartLink.ProductCartLinkId.builder()
                                    .cartId(cart.getId())
                                    .productId(productId)
                                    .build())
                            .quantity(0L)
                            .build();
                        log.debug("link_new cartId={} productId={} initial_qty={}", cart.getId(), productId, 0);
                        return created;
                    });
            link.setQuantity(link.getQuantity() + addQuantity);
            productCartLinkRepository.saveAndFlush(link);
            log.debug("link_update cartId={} productId={} old_qty={} delta={}",
                    cart.getId(), productId, link.getQuantity(), addQuantity);
        }

        // добавляем новые продукты в ShoppingCartDto
        Map<UUID, Long> dtoProducts = productCartLinkRepository
                .findAllById_CartId(cart.getId()).stream()
                .collect(Collectors.toMap(
                        l -> l.getId().getProductId(),
                        ProductCartLink::getQuantity
                ));
        log.debug("done_add_product cartId={}", cart.getId());

        return ShoppingCartDto.builder()
                .cartId(cart.getId())
                .products(dtoProducts)
                .build();
    }

    @Override
    public ShoppingCartDto getCart(String username) {
        // если корзины нет — создаём пустую активную,
        // чтобы тест получил 200 и { products: {} }, а не 404/исключение
        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseGet(() -> cartRepository.saveAndFlush(
                        ShoppingCart.builder()
                                .isActive(true)
                                .username(username)
                                .build()
                ));

        return buildDto(cart.getId());
    }

    @Transactional
    public ResponseEntity<Void> deactivateCart(String username) {
        int changed = cartRepository.deactivate(username);
        if (changed > 0) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> products) {
        UUID cartId = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NoProductsInShoppingCartException(NO_PRODUCTS_IN_CART))
                .getId();

        for (UUID productId : products) {
            if (productCartLinkRepository.deleteById_CartIdAndId_ProductId(cartId, productId) < 1) {
                throw new NoSuchProductInShoppingCartException(
                        String.format(NO_SUCH_PRODUCT_IN_CART, productId, cartId));
            }
        }

        return buildDto(cartId);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, Map<Long, UUID> productsQuantity) {
        UUID cartId = cartRepository.findByUsername(username)
                .orElseGet(() -> cartRepository.saveAndFlush( // если корзины нет, то по ТЗ её нужно создать
                        ShoppingCart.builder()               // и добавить желаемые товары
                                .isActive(true)
                                .username(username)
                                .build()
                )).getId();

        for (Map.Entry<Long, UUID> e : productsQuantity.entrySet()) {
            UUID productId = e.getValue();
            Long newQty = e.getKey();

            // если запись есть — обновляем/удаляем
            Optional<ProductCartLink> linkOpt = productCartLinkRepository
                    .findById_CartIdAndId_ProductId(cartId, productId);
            if (linkOpt.isPresent()) {
                if (newQty == null || newQty <= 0) {
                    productCartLinkRepository.deleteById_CartIdAndId_ProductId(cartId, productId);
                } else {
                    ProductCartLink link = linkOpt.get();
                    link.setQuantity(newQty);
                    productCartLinkRepository.saveAndFlush(link);
                }
                continue;
            }

            // если записи нет — upsert (создаём только при newQty>0)
            if (newQty != null && newQty > 0) {
                ProductCartLink created = ProductCartLink.builder()
                        .cart(ShoppingCart.builder().id(cartId).build())
                        .id(ProductCartLink.ProductCartLinkId.builder()
                                .cartId(cartId)
                                .productId(productId)
                                .build())
                        .quantity(newQty)
                        .build();
                productCartLinkRepository.saveAndFlush(created);
            } // иначе (<=0) — ничего не делаем, и точно не кидаем 400
        }

        return buildDto(cartId);
    }

    private ShoppingCartDto buildDto(UUID cartId) {
        Map<UUID, Long> dtoProducts = productCartLinkRepository
                .findAllById_CartId(cartId)
                .stream()
                .collect(Collectors.toMap(
                        l -> l.getId().getProductId(),
                        ProductCartLink::getQuantity
                ));
        return ShoppingCartDto.builder()
                .cartId(cartId)
                .products(dtoProducts)
                .build();
    }
}