package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String productId;
    private String variantId;
    private String sku;
    private String productNameSnapshot;
    private String imageUrlSnapshot;
    private int quantity;

    // Authoritative price snapshot taken at order time (server-calculated).
    private double unitPriceSnapshot;
    private double lineDiscount;
    private double lineTax;
    private double lineTotal;
}
