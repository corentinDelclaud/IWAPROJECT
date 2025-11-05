package com.iwaproject.test_service.service;

import com.iwaproject.test_service.model.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Product createProduct(Product product) {
        Long id = idGenerator.getAndIncrement();
        product.setId(id);
        products.put(id, product);
        return product;
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public Optional<Product> getProductById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Optional<Product> updateProduct(Long id, Product product) {
        if (products.containsKey(id)) {
            product.setId(id);
            products.put(id, product);
            return Optional.of(product);
        }
        return Optional.empty();
    }

    public boolean deleteProduct(Long id) {
        return products.remove(id) != null;
    }
}
