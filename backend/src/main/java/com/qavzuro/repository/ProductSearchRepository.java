package com.qavzuro.repository;

import com.qavzuro.domain.Product;
import com.qavzuro.dto.request.ProductSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchRepository {
    Page<Product> search(ProductSearchRequest criteria, Pageable pageable);
}
