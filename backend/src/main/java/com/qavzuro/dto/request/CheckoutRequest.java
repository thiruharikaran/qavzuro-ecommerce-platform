package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotBlank private String shippingAddressId;
    @NotBlank private String billingAddressId;
    @NotBlank private String shippingMethod; // "STANDARD", "EXPRESS"
    private String couponCode;
    /** Test/sandbox payment method identifier - never a real card number. */
    private String paymentMethod = "TEST_CARD";
}
