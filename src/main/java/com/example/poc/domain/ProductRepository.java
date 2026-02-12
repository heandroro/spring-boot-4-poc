package com.example.poc.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    Optional<Product> findBySku(String sku);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    List<Product> findByStatus(Product.Status status);

    void delete(Product product);

    void deleteById(String id);

    boolean existsBySku(String sku);
}

