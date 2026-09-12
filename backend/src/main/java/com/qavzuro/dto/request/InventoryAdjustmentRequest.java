package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InventoryAdjustmentRequest {
    private String variantId;
    private int quantityChange; // + or -
    @NotBlank private String reason;
}
