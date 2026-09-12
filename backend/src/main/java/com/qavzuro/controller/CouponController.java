package com.qavzuro.controller;

import com.qavzuro.domain.Coupon;
import com.qavzuro.dto.request.CreateCouponRequest;
import com.qavzuro.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('COUPON_MANAGE')")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<Coupon>> list() {
        return ResponseEntity.ok(couponService.list());
    }

    @PostMapping
    public ResponseEntity<Coupon> create(@Valid @RequestBody CreateCouponRequest req) {
        return ResponseEntity.ok(couponService.create(req));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> setActive(@PathVariable String id, @RequestParam boolean active) {
        couponService.setActive(id, active);
        return ResponseEntity.noContent().build();
    }
}
