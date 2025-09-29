package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.model.Product;

public class ProductMapper {
    public static ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .productName(product.getProductName())
                .productCategory(product.getProductCategory())
                .productState(product.getProductState())
                .productId(product.getProductId())
                .price(product.getPrice())
                .description(product.getDescription())
                .quantityState(product.getQuantityState())
                .imageSrc(product.getImageSrc())
                .build();
    }

    public static Product mapToProduct(ProductDto dto) {
        return Product.builder()
                .productName(dto.getProductName())
                .productCategory(dto.getProductCategory())
                .productState(dto.getProductState())
                .productId(dto.getProductId())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .quantityState(dto.getQuantityState())
                .imageSrc(dto.getImageSrc())
                .build();
    }
}
