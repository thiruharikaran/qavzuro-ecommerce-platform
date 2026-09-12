package com.qavzuro.controller;

import com.qavzuro.domain.Cart;
import com.qavzuro.dto.request.AddToCartRequest;
import com.qavzuro.dto.request.ApplyCouponRequest;
import com.qavzuro.dto.request.UpdateCartItemRequest;
import com.qavzuro.dto.response.CartResponse;
import com.qavzuro.service.CartService;
import com.qavzuro.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest req) {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.addItem(userId, req);
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable String productId,
                                                     @RequestParam(required = false) String variantId,
                                                     @Valid @RequestBody UpdateCartItemRequest req) {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.updateItemQuantity(userId, productId, variantId, req.getQuantity());
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable String productId,
                                                     @RequestParam(required = false) String variantId) {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.removeItem(userId, productId, variantId);
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clear() {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.clear(userId);
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }

    @PostMapping("/coupon")
    public ResponseEntity<CartResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest req) {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.applyCoupon(userId, req.getCode());
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartResponse> removeCoupon() {
        String userId = currentUserService.getUserId();
        Cart cart = cartService.removeCoupon(userId);
        return ResponseEntity.ok(cartService.priceCart(cart, userId));
    }
}
