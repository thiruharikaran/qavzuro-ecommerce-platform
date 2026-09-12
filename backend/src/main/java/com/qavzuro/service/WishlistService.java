package com.qavzuro.service;

import com.qavzuro.domain.Product;
import com.qavzuro.domain.Wishlist;
import com.qavzuro.domain.WishlistItem;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.ProductRepository;
import com.qavzuro.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public Wishlist getOrCreate(String userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().userId(userId).items(new ArrayList<>()).build()));
    }

    public Wishlist add(String userId, String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        Wishlist wishlist = getOrCreate(userId);

        boolean alreadyExists = wishlist.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
        if (alreadyExists) {
            throw new BadRequestException("This product is already in your wishlist.");
        }

        wishlist.getItems().add(WishlistItem.builder()
                .productId(productId)
                .productNameSnapshot(product.getName())
                .imageUrlSnapshot(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl())
                .addedAt(Instant.now())
                .build());
        return wishlistRepository.save(wishlist);
    }

    public Wishlist remove(String userId, String productId) {
        Wishlist wishlist = getOrCreate(userId);
        wishlist.getItems().removeIf(i -> i.getProductId().equals(productId));
        return wishlistRepository.save(wishlist);
    }

    /** Moves an item to the cart (adding it there) and removes it from the wishlist. */
    public void moveToCart(String userId, String productId, CartService cartService) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        var req = new com.qavzuro.dto.request.AddToCartRequest();
        req.setProductId(productId);
        req.setQuantity(1);
        cartService.addItem(userId, req);
        remove(userId, productId);
    }
}
