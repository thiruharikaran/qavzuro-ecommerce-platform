package com.qavzuro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private List<CartLineResponse> items;
    private double subtotal;
    private double discountTotal;
    private double taxTotal;
    private double estimatedShipping;
    private double estimatedTotal;
    private String appliedCouponCode;
    private List<String> warnings; // e.g. "Item X is no longer available"

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartLineResponse {
        private String productId;
        private String variantId;
        private String name;
        private String imageUrl;
        private int quantity;
        private double unitPrice;
        private double lineTotal;
        private boolean available;
        private int availableQuantity;
    }
}
