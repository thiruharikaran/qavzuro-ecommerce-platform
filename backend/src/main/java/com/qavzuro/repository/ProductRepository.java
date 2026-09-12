package com.qavzuro.repository;

import com.qavzuro.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String>, ProductSearchRepository {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);

    @Query("{ 'inventoryQuantity': { $lte: ?0 } }")
    java.util.List<Product> findLowStock(int threshold);
}
