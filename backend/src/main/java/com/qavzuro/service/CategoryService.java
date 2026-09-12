package com.qavzuro.service;

import com.qavzuro.domain.Category;
import com.qavzuro.dto.request.CreateCategoryRequest;
import com.qavzuro.exception.ConflictException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> listActive() {
        return categoryRepository.findByActiveTrue();
    }

    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    public Category getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    public Category getById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    public List<Category> listSubcategories(String parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Category create(CreateCategoryRequest req) {
        if (categoryRepository.findBySlug(req.getSlug()).isPresent()) {
            throw new ConflictException("A category with this slug already exists.");
        }
        Category category = Category.builder()
                .slug(req.getSlug())
                .name(req.getName())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .parentId(req.getParentId())
                .active(true)
                .build();
        return categoryRepository.save(category);
    }

    public Category update(String id, CreateCategoryRequest req) {
        Category category = getById(id);
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setImageUrl(req.getImageUrl());
        category.setParentId(req.getParentId());
        return categoryRepository.save(category);
    }

    public void setActive(String id, boolean active) {
        Category category = getById(id);
        category.setActive(active);
        categoryRepository.save(category);
    }
}
