package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.request.AddToCartRequest;
import com.qavzuro.dto.response.CartResponse;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.repository.CartRepository;
import com.qavzuro.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Covers server-authoritative cart pricing - the frontend never supplies prices or totals. */
class CartServiceTest {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CouponService couponService;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartRepository = mock(CartRepository.class);
        productRepository = mock(ProductRepository.class);
        couponService = mock(CouponService.class);
        cartService = new CartService(cartRepository, productRepository, couponService);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void addItem_rejectsInactiveProduct() {
        Product product = Product.builder().id("p1").name("Widget").status(ProductStatus.DRAFT).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.empty());

        AddToCartRequest req = new AddToCartRequest();
        req.setProductId("p1");
        req.setQuantity(1);

        assertThrows(BadRequestException.class, () -> cartService.addItem("u1", req));
    }

    @Test
    void priceCart_usesLiveProductPrice_notAnyStoredClientValue() {
        Product product = Product.builder().id("p1").name("Widget").status(ProductStatus.ACTIVE)
                .price(100).inventoryQuantity(50).images(List.of()).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        Cart cart = Cart.builder().userId("u1").items(new ArrayList<>(List.of(
                CartItem.builder().productId("p1").quantity(2).productNameSnapshot("Widget (stale name)").build()
        ))).build();

        CartResponse response = cartService.priceCart(cart, null);

        assertEquals(200.0, response.getSubtotal(), "Subtotal must be computed from the live product price (100 x 2), not a stale snapshot");
        assertEquals("Widget", response.getItems().get(0).getName(), "Display fields should reflect current product data");
    }

    @Test
    void priceCart_appliesSalePriceWhenLowerThanRegularPrice() {
        Product product = Product.builder().id("p1").name("Widget").status(ProductStatus.ACTIVE)
                .price(100).salePrice(80.0).inventoryQuantity(50).images(List.of()).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        Cart cart = Cart.builder().userId("u1").items(new ArrayList<>(List.of(
                CartItem.builder().productId("p1").quantity(1).build()
        ))).build();

        CartResponse response = cartService.priceCart(cart, null);
        assertEquals(80.0, response.getSubtotal());
    }

    @Test
    void priceCart_flagsUnavailableProduct_withoutThrowing() {
        Cart cart = Cart.builder().userId("u1").items(new ArrayList<>(List.of(
                CartItem.builder().productId("deleted-product").quantity(1).productNameSnapshot("Ghost Item").build()
        ))).build();
        when(productRepository.findById("deleted-product")).thenReturn(Optional.empty());

        CartResponse response = cartService.priceCart(cart, null);

        assertFalse(response.getItems().get(0).isAvailable());
        assertFalse(response.getWarnings().isEmpty());
        assertEquals(0.0, response.getSubtotal());
    }

    @Test
    void priceCart_flagsInsufficientStock() {
        Product product = Product.builder().id("p1").name("Widget").status(ProductStatus.ACTIVE)
                .price(50).inventoryQuantity(1).images(List.of()).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        Cart cart = Cart.builder().userId("u1").items(new ArrayList<>(List.of(
                CartItem.builder().productId("p1").quantity(5).build()
        ))).build();

        CartResponse response = cartService.priceCart(cart, null);
        assertFalse(response.getItems().get(0).isAvailable());
    }
}
