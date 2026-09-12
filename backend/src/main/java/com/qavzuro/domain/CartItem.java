package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private String variantId; // nullable
    private int quantity;

    // Denormalized snapshot for display only - NEVER trusted for pricing.
    private String productNameSnapshot;
    private String imageUrlSnapshot;
}
