package com.qavzuro.service;

import com.qavzuro.domain.Coupon;
import com.qavzuro.domain.DiscountType;
import com.qavzuro.domain.Order;
import com.qavzuro.dto.request.CreateCouponRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ConflictException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.CouponRepository;
import com.qavzuro.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    public Coupon create(CreateCouponRequest req) {
        if (couponRepository.findByCode(req.getCode()).isPresent()) {
            throw new ConflictException("A coupon with this code already exists.");
        }
        Coupon coupon = Coupon.builder()
                .code(req.getCode().toUpperCase())
                .discountType(req.getDiscountType())
                .discountValue(req.getDiscountValue())
                .minimumOrderValue(req.getMinimumOrderValue())
                .maximumDiscountAmount(req.getMaximumDiscountAmount())
                .startDate(req.getStartDate())
                .expirationDate(req.getExpirationDate())
                .usageLimit(req.getUsageLimit())
                .perUserLimit(req.getPerUserLimit())
                .applicableCategoryIds(req.getApplicableCategoryIds() != null ? req.getApplicableCategoryIds() : new ArrayList<>())
                .applicableProductIds(req.getApplicableProductIds() != null ? req.getApplicableProductIds() : new ArrayList<>())
                .active(true)
                .build();
        return couponRepository.save(coupon);
    }

    public List<Coupon> list() {
        return couponRepository.findAll();
    }

    public void setActive(String id, boolean active) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coupon not found."));
        coupon.setActive(active);
        couponRepository.save(coupon);
    }

    /**
     * Validates a coupon against server-known facts only (never trusts any
     * discount amount from the client) and returns the discount to apply to
     * the given subtotal, or throws if the coupon cannot be applied.
     */
    public double validateAndCalculateDiscount(String code, String userId, double subtotal) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid coupon code."));

        if (!coupon.isActive()) {
            throw new BadRequestException("This coupon is no longer active.");
        }
        Instant now = Instant.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new BadRequestException("This coupon is not yet valid.");
        }
        if (coupon.getExpirationDate() != null && now.isAfter(coupon.getExpirationDate())) {
            throw new BadRequestException("This coupon has expired.");
        }
        if (coupon.getMinimumOrderValue() != null && subtotal < coupon.getMinimumOrderValue()) {
            throw new BadRequestException("Order subtotal does not meet the coupon's minimum value.");
        }
        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new BadRequestException("This coupon has reached its usage limit.");
        }
        if (coupon.getPerUserLimit() != null) {
            long usedByUser = orderRepository.findByCustomerId(userId, org.springframework.data.domain.Pageable.unpaged())
                    .stream().filter(o -> code.equalsIgnoreCase(o.getAppliedCouponCode())).count();
            if (usedByUser >= coupon.getPerUserLimit()) {
                throw new BadRequestException("You have already used this coupon the maximum number of times.");
            }
        }

        double discount = coupon.getDiscountType() == DiscountType.PERCENTAGE
                ? subtotal * (coupon.getDiscountValue() / 100.0)
                : coupon.getDiscountValue();

        if (coupon.getMaximumDiscountAmount() != null) {
            discount = Math.min(discount, coupon.getMaximumDiscountAmount());
        }
        return Math.min(discount, subtotal);
    }

    public void incrementUsage(String code) {
        couponRepository.findByCode(code.toUpperCase()).ifPresent(c -> {
            c.setTimesUsed(c.getTimesUsed() + 1);
            couponRepository.save(c);
        });
    }
}
