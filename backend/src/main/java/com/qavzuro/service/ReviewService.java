package com.qavzuro.service;

import com.qavzuro.domain.*;
import com.qavzuro.dto.request.CreateReviewRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.OrderRepository;
import com.qavzuro.repository.ProductRepository;
import com.qavzuro.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public Review create(String userId, String userDisplayName, String productId, CreateReviewRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        if (reviewRepository.countByProductIdAndCustomerId(productId, userId) > 0) {
            throw new BadRequestException("You have already reviewed this product.");
        }

        boolean verified = orderRepository.findByCustomerId(userId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getItems().stream())
                .anyMatch(item -> item.getProductId().equals(productId));

        Review review = Review.builder()
                .productId(productId)
                .customerId(userId)
                .customerDisplayName(userDisplayName)
                .rating(req.getRating())
                .title(req.getTitle())
                .reviewText(req.getReviewText())
                .verifiedPurchase(verified)
                .moderationStatus(ReviewModerationStatus.PENDING)
                .build();
        Review saved = reviewRepository.save(review);

        recalculateProductRating(productId);
        return saved;
    }

    public Page<Review> listForProduct(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndModerationStatus(productId, ReviewModerationStatus.APPROVED, pageable);
    }

    public Page<Review> listPendingModeration(Pageable pageable) {
        return reviewRepository.findByModerationStatus(ReviewModerationStatus.PENDING, pageable);
    }

    public Review moderate(String staffUserId, String reviewId, ReviewModerationStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found."));
        review.setModerationStatus(status);
        review.setModeratedByUserId(staffUserId);
        Review saved = reviewRepository.save(review);
        recalculateProductRating(review.getProductId());
        return saved;
    }

    private void recalculateProductRating(String productId) {
        var approved = reviewRepository.findByProductIdAndModerationStatus(
                productId, ReviewModerationStatus.APPROVED, org.springframework.data.domain.Pageable.unpaged());
        double avg = approved.stream().mapToInt(Review::getRating).average().orElse(0.0);
        long count = approved.getTotalElements();

        productRepository.findById(productId).ifPresent(product -> {
            product.setRatingSummary(RatingSummary.builder()
                    .average(Math.round(avg * 10.0) / 10.0)
                    .count(count)
                    .build());
            productRepository.save(product);
        });
    }
}
