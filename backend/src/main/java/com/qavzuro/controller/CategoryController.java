package com.qavzuro.controller;

import com.qavzuro.domain.Category;
import com.qavzuro.dto.request.CreateCategoryRequest;
import com.qavzuro.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> listActive() {
        return ResponseEntity.ok(categoryService.listActive());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Category> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<Category>> subcategories(@PathVariable String parentId) {
        return ResponseEntity.ok(categoryService.listSubcategories(parentId));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ResponseEntity<List<Category>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ResponseEntity<Category> create(@Valid @RequestBody CreateCategoryRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ResponseEntity<Category> update(@PathVariable String id, @Valid @RequestBody CreateCategoryRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    public ResponseEntity<Void> setActive(@PathVariable String id, @RequestParam boolean active) {
        categoryService.setActive(id, active);
        return ResponseEntity.noContent().build();
    }
}
