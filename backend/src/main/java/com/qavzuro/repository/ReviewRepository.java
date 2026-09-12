package com.qavzuro.repository;

import com.qavzuro.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review, String> {
    Page<Review> findByProductIdAndModerationStatus(String productId, com.qavzuro.domain.ReviewModerationStatus status, Pageable pageable);
    long countByProductIdAndCustomerId(String productId, String customerId);
    Page<Review> findByModerationStatus(com.qavzuro.domain.ReviewModerationStatus status, Pageable pageable);
}
