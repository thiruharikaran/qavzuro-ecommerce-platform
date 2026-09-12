package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateProductRequest {
    @NotBlank private String sku;
    @NotBlank private String name;
    private String description;
    private String shortDescription;
    private String brand;
    @NotBlank private String categoryId;
    private String subcategoryId;

    @PositiveOrZero private double price;
    private Double salePrice;
    private String currency;
    private double taxRatePercent;

    @PositiveOrZero private int inventoryQuantity;
    private int lowStockThreshold;

    private List<Map<String, String>> images; // [{url, altText}]
    private Map<String, String> specifications;
    private Map<String, String> attributes;
    private List<String> tags;
}
