package com.techie.microservices.product.service;

import com.techie.microservices.product.dto.ProductRequest;
import com.techie.microservices.product.dto.ProductResponse;
import com.techie.microservices.product.exception.DatabaseException;
import com.techie.microservices.product.model.Product;
import com.techie.microservices.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .skuCode(productRequest.skuCode())
                .price(productRequest.price())
                .build();

        productRepository.save(product);
        logger.info("Product create successfully");

        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getSkuCode(),
                product.getPrice());
    }

    public List<ProductResponse> getAllProducts() {
        logger.info("Iniciando la búsqueda de productos...");
        try {
            List<Product> products = productRepository.findAll();

            if (products.isEmpty()) {
                logger.info("No se encontraron productos en la base de datos.");
                return Collections.emptyList();
            }
            List<ProductResponse> response = products.stream()
                    .map(this::mapToProductResponse)
                    .collect(Collectors.toList());

            logger.info("Mapeo completado exitosamente, items: {}", response.size());

            return response;
        } catch (Exception e) {
            logger.error("Error al recuperar productos: {}", e.getMessage());

            throw new DatabaseException("Error de conexión o consulta en MongoDB " + e.getMessage());
        }
    }


    public Product updateProduct(String productId, Product updatedProduct) {
        Product product = productRepository.findById(productId).orElseThrow();

        if (product != null) {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setSkuCode(updatedProduct.getSkuCode());
            product.setPrice(updatedProduct.getPrice());
            return productRepository.save(product);
        }

        return null;
    }


    public boolean deleteProduct(String productId) {
        Product product = productRepository.findById(productId).orElseThrow();

        if (product != null) {
            productRepository.delete(product);
            return true;
        }
        else {
            return false;
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                String.valueOf(product.getId()),
                product.getName(),
                product.getDescription(),
                product.getSkuCode(),
                product.getPrice()
        );
    }
}
