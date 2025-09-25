package ru.yandex.practicum.service;

import jakarta.transaction.Transactional;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductDto addProduct(ProductDto product) {
        return ProductMapper.mapToDto(productRepository.save(ProductMapper.mapToProduct(product)));
    }

    public ProductDto updateProduct(ProductDto pNew) {
        Product p = productRepository.findById(pNew.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product with ID %s not found", pNew.getProductId())));
        updateProduct(p, ProductMapper.mapToProduct(pNew));
        return ProductMapper.mapToDto(productRepository.save(p));
    }

    public Page<ProductDto> getProduct(ProductCategory category, Pageable pageable) {
        return productRepository.findByProductCategory(category, pageable)
                .map(ProductMapper::mapToDto);
    }

    @Transactional
    public boolean removeProduct(UUID productId) {
        return productRepository.findById(productId).map(product -> {
            product.setProductState(ProductState.DEACTIVATE);
            productRepository.save(product);
            return true;
        }).orElse(false);
    }

    public boolean setProductState(UUID productId, QuantityState quantityState) {
        Product foundProduct;
        try {
            foundProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            String.format("Product with ID %s not found", productId)));
        } catch (ProductNotFoundException ex) {
            log.error("Not found", ex);
            return false;
        }
        foundProduct.setQuantityState(quantityState);
        productRepository.save(foundProduct);
        return true;
    }

    public ProductDto getProductById(UUID productId) {
        Product foundProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            String.format("Product with ID %s not found", productId)));
        return ProductMapper.mapToDto(foundProduct);
    }

    private void updateProduct(Product pOld, Product pNew) {
        if (pNew.getProductName() != null) {
            pOld.setProductName(pNew.getProductName());
        }
        if (pNew.getProductState() != null) {
            pOld.setProductState(pNew.getProductState());
        }
        if (pNew.getProductCategory() != null) {
            pOld.setProductCategory(pNew.getProductCategory());
        }
        if (pNew.getPrice() != null) {
            pOld.setPrice(pNew.getPrice());
        }
        if (pNew.getDescription() != null) {
            pOld.setDescription(pNew.getDescription());
        }
        if (pNew.getImageSrc() != null) {
            pOld.setImageSrc(pNew.getImageSrc());
        }
        if (pNew.getQuantityState() != null) {
            pOld.setQuantityState(pNew.getQuantityState());
        }
    }

}
