package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private String id;

    @Indexed
    private String productId;
    @Indexed
    private String customerId;
    private String customerDisplayName;

    private int rating; // 1-5
    private String title;
    private String reviewText;

    @Builder.Default
    private boolean verifiedPurchase = false;

    @Builder.Default
    private ReviewModerationStatus moderationStatus = ReviewModerationStatus.PENDING;
    private String moderatedByUserId;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
