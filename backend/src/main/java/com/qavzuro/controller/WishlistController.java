package com.qavzuro.controller;

import com.qavzuro.domain.Wishlist;
import com.qavzuro.service.CartService;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<Wishlist> get() {
        return ResponseEntity.ok(wishlistService.getOrCreate(currentUserService.getUserId()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Wishlist> add(@PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.add(currentUserService.getUserId(), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Wishlist> remove(@PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.remove(currentUserService.getUserId(), productId));
    }

    @PostMapping("/{productId}/move-to-cart")
    public ResponseEntity<Void> moveToCart(@PathVariable String productId) {
        wishlistService.moveToCart(currentUserService.getUserId(), productId, cartService);
        return ResponseEntity.noContent().build();
    }
}
