package ru.yandex.practicum.service;

import jakarta.ws.rs.NotFoundException;
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

    public ProductDto addProduct(Product product) {
        return ProductMapper.mapProductToDto(productRepository.save(product));
    }

    public ProductDto updateProduct(Product pNew) {
        Product p = productRepository.findById(pNew.getProductId())
                .orElseThrow(NotFoundException::new);
        updateProduct(p, pNew);
        return ProductMapper.mapProductToDto(productRepository.save(p));
    }

    public Page<ProductDto> getProduct(ProductCategory category, Pageable pageable) {
        return productRepository.findByProductCategory(category, pageable)
                .map(ProductMapper::mapProductToDto);
    }

    public boolean removeProduct(UUID productId) {
        try {
            productRepository.deleteById(productId);
            return true;
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
    }

    public boolean setProductState(UUID productId, QuantityState quantityState) {
        Product foundProduct;
        try {
            foundProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Product with ID %s not found", productId)));
        } catch (NotFoundException ex) {
            log.error("Not found", ex);
            return false;
        }
        foundProduct.setQuantityState(quantityState);
        productRepository.save(foundProduct);
        return true;
    }

    public ProductDto getProductById(UUID productId) {
        Product foundProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Product with ID %s not found", productId)));
        return ProductMapper.mapProductToDto(foundProduct);
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
