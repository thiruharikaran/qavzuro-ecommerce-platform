package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "coupons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private DiscountType discountType;
    private double discountValue; // percentage (0-100) or fixed amount
    private Double minimumOrderValue;
    private Double maximumDiscountAmount;

    private Instant startDate;
    private Instant expirationDate;

    private Integer usageLimit;      // total uses allowed, null = unlimited
    private Integer perUserLimit;    // uses per user, null = unlimited
    @Builder.Default
    private int timesUsed = 0;

    @Builder.Default
    private boolean active = true;

    /** Empty lists mean "applies to everything". */
    @Builder.Default
    private List<String> applicableCategoryIds = new ArrayList<>();
    @Builder.Default
    private List<String> applicableProductIds = new ArrayList<>();
}
