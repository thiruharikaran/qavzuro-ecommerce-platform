package com.qavzuro.service;

import com.qavzuro.domain.Cart;
import com.qavzuro.domain.CartItem;
import com.qavzuro.domain.Product;
import com.qavzuro.domain.ProductStatus;
import com.qavzuro.domain.ProductVariant;
import com.qavzuro.dto.request.AddToCartRequest;
import com.qavzuro.dto.response.CartResponse;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.InsufficientStockException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.CartRepository;
import com.qavzuro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The cart stores only productId/variantId/quantity. Every price shown to the
 * user, and every total, is computed fresh from the current Product record on
 * every read - the frontend never supplies or is trusted for pricing.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponService couponService;

    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).items(new ArrayList<>()).build()));
    }

    public Cart addItem(String userId, AddToCartRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("This product is not currently available.");
        }
        validateVariant(product, req.getVariantId());

        Cart cart = getOrCreateCart(userId);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.getProductId()) && java.util.Objects.equals(i.getVariantId(), req.getVariantId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + req.getQuantity());
        } else {
            cart.getItems().add(CartItem.builder()
                    .productId(req.getProductId())
                    .variantId(req.getVariantId())
                    .quantity(req.getQuantity())
                    .productNameSnapshot(product.getName())
                    .imageUrlSnapshot(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl())
                    .build());
        }
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String userId, String productId, String variantId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        if (quantity <= 0) {
            cart.getItems().removeIf(i -> i.getProductId().equals(productId) && java.util.Objects.equals(i.getVariantId(), variantId));
        } else {
            cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId) && java.util.Objects.equals(i.getVariantId(), variantId))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> item.setQuantity(quantity),
                            () -> { throw new ResourceNotFoundException("Item not found in cart."); }
                    );
        }
        return cartRepository.save(cart);
    }

    public Cart removeItem(String userId, String productId, String variantId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId) && java.util.Objects.equals(i.getVariantId(), variantId));
        return cartRepository.save(cart);
    }

    public Cart clear(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cart.setAppliedCouponCode(null);
        return cartRepository.save(cart);
    }

    public Cart applyCoupon(String userId, String code) {
        Cart cart = getOrCreateCart(userId);
        CartResponse priced = priceCart(cart, null); // validate current subtotal first
        couponService.validateAndCalculateDiscount(code, userId, priced.getSubtotal()); // throws if invalid
        cart.setAppliedCouponCode(code.toUpperCase());
        return cartRepository.save(cart);
    }

    public Cart removeCoupon(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.setAppliedCouponCode(null);
        return cartRepository.save(cart);
    }

    /** Recomputes authoritative pricing for a cart from live product data. */
    public CartResponse priceCart(Cart cart, String userIdForCouponValidation) {
        List<CartResponse.CartLineResponse> lines = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        double subtotal = 0;

        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
                warnings.add((item.getProductNameSnapshot() != null ? item.getProductNameSnapshot() : "An item") + " is no longer available.");
                lines.add(CartResponse.CartLineResponse.builder()
                        .productId(item.getProductId()).variantId(item.getVariantId())
                        .name(item.getProductNameSnapshot()).imageUrl(item.getImageUrlSnapshot())
                        .quantity(item.getQuantity()).unitPrice(0).lineTotal(0)
                        .available(false).availableQuantity(0)
                        .build());
                continue;
            }

            double unitPrice = product.getEffectivePrice();
            int availableQty = product.getAvailableQuantity();

            if (item.getVariantId() != null) {
                Optional<ProductVariant> variantOpt = product.getVariants().stream()
                        .filter(v -> v.getVariantId().equals(item.getVariantId())).findFirst();
                if (variantOpt.isEmpty()) {
                    warnings.add(product.getName() + " variant is no longer available.");
                    continue;
                }
                ProductVariant variant = variantOpt.get();
                if (variant.getPriceOverride() != null) unitPrice = variant.getPriceOverride();
                availableQty = Math.max(0, variant.getStockQuantity() - variant.getReservedQuantity());
            }

            boolean available = availableQty >= item.getQuantity();
            if (!available) {
                warnings.add(product.getName() + " has limited stock (" + availableQty + " available).");
            }

            double lineTotal = unitPrice * item.getQuantity();
            subtotal += lineTotal;

            lines.add(CartResponse.CartLineResponse.builder()
                    .productId(product.getId())
                    .variantId(item.getVariantId())
                    .name(product.getName())
                    .imageUrl(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .available(available)
                    .availableQuantity(availableQty)
                    .build());
        }

        double discount = 0;
        if (cart.getAppliedCouponCode() != null && userIdForCouponValidation != null) {
            try {
                discount = couponService.validateAndCalculateDiscount(cart.getAppliedCouponCode(), userIdForCouponValidation, subtotal);
            } catch (Exception e) {
                warnings.add("Coupon '" + cart.getAppliedCouponCode() + "' is no longer valid and was not applied.");
            }
        }

        double taxableAmount = Math.max(0, subtotal - discount);
        double tax = taxableAmount * 0.0; // per-line tax is computed authoritatively at checkout using each product's tax rate
        double estimatedShipping = subtotal > 0 ? 0 : 0; // finalized at checkout once a shipping method is chosen

        return CartResponse.builder()
                .items(lines)
                .subtotal(round(subtotal))
                .discountTotal(round(discount))
                .taxTotal(round(tax))
                .estimatedShipping(round(estimatedShipping))
                .estimatedTotal(round(subtotal - discount + tax + estimatedShipping))
                .appliedCouponCode(cart.getAppliedCouponCode())
                .warnings(warnings)
                .build();
    }

    private void validateVariant(Product product, String variantId) {
        if (variantId == null || variantId.isBlank()) return;
        boolean exists = product.getVariants().stream().anyMatch(v -> v.getVariantId().equals(variantId));
        if (!exists) throw new ResourceNotFoundException("Product variant not found.");
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
