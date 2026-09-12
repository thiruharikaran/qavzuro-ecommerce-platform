package com.qavzuro.dto.request;

import com.qavzuro.domain.ProductStatus;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private String shortDescription;
    private String brand;
    private String categoryId;
    private String subcategoryId;
    private Double price;
    private Double salePrice;
    private Double taxRatePercent;
    private Integer lowStockThreshold;
    private ProductStatus status;
    private List<Map<String, String>> images;
    private Map<String, String> specifications;
    private Map<String, String> attributes;
    private List<String> tags;
}
