package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;

    @Indexed(unique = true)
    private String slug;

    @Indexed
    private String name;

    private String description;
    private String shortDescription;
    private String brand;

    @Indexed
    private String categoryId;
    private String subcategoryId;

    /** Base price; variants may override. */
    private double price;
    private Double salePrice;
    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private double taxRatePercent = 0.0;

    @Builder.Default
    private int inventoryQuantity = 0;
    @Builder.Default
    private int reservedQuantity = 0;
    @Builder.Default
    private int lowStockThreshold = 5;

    @Indexed
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    private Map<String, String> specifications = new HashMap<>();

    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @Indexed
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private RatingSummary ratingSummary = new RatingSummary();

    @Field("popularityScore")
    @Builder.Default
    private double popularityScore = 0.0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public double getEffectivePrice() {
        return (salePrice != null && salePrice > 0 && salePrice < price) ? salePrice : price;
    }

    public int getAvailableQuantity() {
        return Math.max(0, inventoryQuantity - reservedQuantity);
    }
}
