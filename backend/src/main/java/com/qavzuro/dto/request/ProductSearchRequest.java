package com.qavzuro.dto.request;

import com.qavzuro.domain.ProductStatus;
import lombok.Data;

@Data
public class ProductSearchRequest {
    private String keyword;
    private String categoryId;
    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private Boolean inStockOnly;
    private ProductStatus status; // admin-only override

    private int page = 0;
    private int size = 20;
    private String sort = "relevance"; // relevance|newest|price_asc|price_desc|rating|popularity
}
