package com.qavzuro.service;

import com.qavzuro.domain.Coupon;
import com.qavzuro.domain.DiscountType;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.repository.CouponRepository;
import com.qavzuro.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Covers server-side coupon validation - the discount must always be derived from server data, never the client. */
class CouponServiceTest {

    private CouponRepository couponRepository;
    private OrderRepository orderRepository;
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        orderRepository = mock(OrderRepository.class);
        when(orderRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<>(List.of()));
        couponService = new CouponService(couponRepository, orderRepository);
    }

    @Test
    void appliesPercentageDiscount_cappedByMaximum() {
        Coupon coupon = Coupon.builder().code("SAVE20").discountType(DiscountType.PERCENTAGE)
                .discountValue(20).maximumDiscountAmount(100.0).active(true).build();
        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.of(coupon));

        double discount = couponService.validateAndCalculateDiscount("SAVE20", "user-1", 1000.0);

        assertEquals(100.0, discount, "20% of 1000 is 200, but must be capped at the max discount of 100");
    }

    @Test
    void appliesFixedDiscount_neverExceedingSubtotal() {
        Coupon coupon = Coupon.builder().code("FLAT500").discountType(DiscountType.FIXED)
                .discountValue(500).active(true).build();
        when(couponRepository.findByCode("FLAT500")).thenReturn(Optional.of(coupon));

        double discount = couponService.validateAndCalculateDiscount("FLAT500", "user-1", 300.0);

        assertEquals(300.0, discount, "A fixed discount larger than the subtotal must be clamped to the subtotal");
    }

    @Test
    void rejectsExpiredCoupon() {
        Coupon coupon = Coupon.builder().code("OLD10").discountType(DiscountType.PERCENTAGE).discountValue(10)
                .expirationDate(Instant.now().minusSeconds(3600)).active(true).build();
        when(couponRepository.findByCode("OLD10")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () -> couponService.validateAndCalculateDiscount("OLD10", "user-1", 100.0));
    }

    @Test
    void rejectsWhenBelowMinimumOrderValue() {
        Coupon coupon = Coupon.builder().code("BIG50").discountType(DiscountType.FIXED).discountValue(50)
                .minimumOrderValue(500.0).active(true).build();
        when(couponRepository.findByCode("BIG50")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () -> couponService.validateAndCalculateDiscount("BIG50", "user-1", 100.0));
    }

    @Test
    void rejectsWhenUsageLimitReached() {
        Coupon coupon = Coupon.builder().code("LIMITED").discountType(DiscountType.FIXED).discountValue(10)
                .usageLimit(5).timesUsed(5).active(true).build();
        when(couponRepository.findByCode("LIMITED")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () -> couponService.validateAndCalculateDiscount("LIMITED", "user-1", 100.0));
    }

    @Test
    void rejectsInactiveCoupon() {
        Coupon coupon = Coupon.builder().code("PAUSED").discountType(DiscountType.FIXED).discountValue(10).active(false).build();
        when(couponRepository.findByCode("PAUSED")).thenReturn(Optional.of(coupon));

        assertThrows(BadRequestException.class, () -> couponService.validateAndCalculateDiscount("PAUSED", "user-1", 100.0));
    }

    @Test
    void rejectsUnknownCode() {
        when(couponRepository.findByCode("NOPE")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> couponService.validateAndCalculateDiscount("NOPE", "user-1", 100.0));
    }
}
