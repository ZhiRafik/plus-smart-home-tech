package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.model.Product;

import java.util.UUID;

public interface ProductService {
    ProductDto addProduct(ProductDto product);

    ProductDto updateProduct(ProductDto product);

    Page<ProductDto> getProduct(ProductCategory category, Pageable pageable);

    boolean removeProduct(UUID productId);

    boolean setProductState(UUID productId, QuantityState quantityState);

    ProductDto getProductById(UUID productId);
}
