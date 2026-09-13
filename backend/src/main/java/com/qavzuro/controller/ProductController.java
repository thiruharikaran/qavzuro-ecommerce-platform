package com.qavzuro.controller;

import com.qavzuro.domain.Product;
import com.qavzuro.dto.request.CreateProductRequest;
import com.qavzuro.dto.request.ProductSearchRequest;
import com.qavzuro.dto.request.UpdateProductRequest;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CurrentUserService currentUserService;

    /** Public catalog listing/search - supports filters, sorting, and pagination. */
@GetMapping
public ResponseEntity<Page<Product>> list(ProductSearchRequest req) {
    return ResponseEntity.ok(productService.search(req));
}

/** Public catalog search - keyword, category, brand, price/rating filters, sort, pagination. */
@GetMapping("/search")
public ResponseEntity<Page<Product>> search(ProductSearchRequest req) {
    return ResponseEntity.ok(productService.search(req));
}

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Product> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<Product>> related(@PathVariable String id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(productService.relatedProducts(product, 8));
    }

    @GetMapping("/admin/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<List<Product>> lowStock() {
        return ResponseEntity.ok(productService.lowStock());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.ok(productService.create(currentUserService.getUserId(), req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<Product> update(@PathVariable String id, @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(productService.update(currentUserService.getUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(currentUserService.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
