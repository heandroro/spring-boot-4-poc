package com.example.poc.infrastructure.persistence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.poc.domain.Product;
import com.example.poc.domain.ProductRepository;
@Repository
public interface MongoProductRepository extends MongoRepository<Product, String>, ProductRepository {
    Optional<Product> findBySku(String sku);
    Page<Product> findByCategory(String category, Pageable pageable);
    List<Product> findByStatus(Product.Status status);
    boolean existsBySku(String sku);
}
