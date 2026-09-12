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
import java.util.ArrayList;
import java.util.List;

@Document(collection = "return_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {

    @Id
    private String id;

    @Indexed
    private String orderId;
    @Indexed
    private String customerId;

    @Builder.Default
    private List<String> orderItemProductIds = new ArrayList<>();
    private String reason;
    private String customerNote;

    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;

    private String reviewedByUserId;
    private String reviewNote;

    private Double refundAmount;
    private String refundReference;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
