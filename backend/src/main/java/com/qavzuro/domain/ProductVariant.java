package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * A purchasable configuration of a product (e.g. size=M, color=Red).
 * Each variant has its own SKU, price override, and stock so different
 * configurations can sell out independently.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    private String variantId;
    private String sku;

    /** e.g. {"size": "M", "color": "Red"} */
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    private Double priceOverride; // null => use parent product price
    @Builder.Default
    private int stockQuantity = 0;
    @Builder.Default
    private int reservedQuantity = 0;
}
