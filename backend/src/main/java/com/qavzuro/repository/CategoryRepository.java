package com.qavzuro.repository;

import com.qavzuro.domain.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentId(String parentId);
    List<Category> findByActiveTrue();
}
