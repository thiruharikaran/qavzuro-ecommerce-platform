package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.request.CreateReviewRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.repository.OrderRepository;
import com.qavzuro.repository.ProductRepository;
import com.qavzuro.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Covers verified-purchase detection and duplicate-review prevention. */
class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        reviewService = new ReviewService(reviewRepository, productRepository, orderRepository);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            if (r.getId() == null) r.setId("review-1");
            return r;
        });
        when(reviewRepository.findByProductIdAndModerationStatus(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
    }

    @Test
    void create_marksVerifiedPurchase_whenCustomerHasDeliveredOrderContainingProduct() {
        Product product = Product.builder().id("p1").name("Widget").build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(reviewRepository.countByProductIdAndCustomerId("p1", "u1")).thenReturn(0L);

        Order deliveredOrder = Order.builder().customerId("u1").status(OrderStatus.DELIVERED)
                .items(List.of(OrderItem.builder().productId("p1").build())).build();
        when(orderRepository.findByCustomerId(eq("u1"), any())).thenReturn(new PageImpl<>(List.of(deliveredOrder)));

        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating(5); req.setTitle("Great"); req.setReviewText("Loved it");

        Review saved = reviewService.create("u1", "A. Customer", "p1", req);

        assertTrue(saved.isVerifiedPurchase(), "Review should be marked verified when the customer has a delivered order for this product");
    }

    @Test
    void create_doesNotMarkVerified_whenNoDeliveredOrderExists() {
        Product product = Product.builder().id("p1").name("Widget").build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(reviewRepository.countByProductIdAndCustomerId("p1", "u1")).thenReturn(0L);
        when(orderRepository.findByCustomerId(eq("u1"), any())).thenReturn(new PageImpl<>(List.of()));

        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating(4); req.setTitle("Ok"); req.setReviewText("It's fine");

        Review saved = reviewService.create("u1", "A. Customer", "p1", req);

        assertFalse(saved.isVerifiedPurchase());
    }

    @Test
    void create_rejectsSecondReviewFromSameCustomerForSameProduct() {
        Product product = Product.builder().id("p1").name("Widget").build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(reviewRepository.countByProductIdAndCustomerId("p1", "u1")).thenReturn(1L);

        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating(3); req.setTitle("Again"); req.setReviewText("Trying to review twice");

        assertThrows(BadRequestException.class, () -> reviewService.create("u1", "A. Customer", "p1", req));
    }

    @Test
    void newReview_startsInPendingModeration() {
        Product product = Product.builder().id("p1").name("Widget").build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(reviewRepository.countByProductIdAndCustomerId("p1", "u1")).thenReturn(0L);
        when(orderRepository.findByCustomerId(eq("u1"), any())).thenReturn(new PageImpl<>(List.of()));

        CreateReviewRequest req = new CreateReviewRequest();
        req.setRating(5); req.setTitle("Nice"); req.setReviewText("Good product");

        Review saved = reviewService.create("u1", "A. Customer", "p1", req);
        assertEquals(ReviewModerationStatus.PENDING, saved.getModerationStatus());
    }
}
