package com.qavzuro.dto.request;

import com.qavzuro.domain.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreateCouponRequest {
    @NotBlank private String code;
    @NotNull private DiscountType discountType;
    private double discountValue;
    private Double minimumOrderValue;
    private Double maximumDiscountAmount;
    private Instant startDate;
    private Instant expirationDate;
    private Integer usageLimit;
    private Integer perUserLimit;
    private List<String> applicableCategoryIds;
    private List<String> applicableProductIds;
}
