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

/**
 * An operational task assignable to workforce users (Worker/Cleaner) and
 * assigned/monitored by Supervisor/Team Lead/Manager roles.
 */
@Document(collection = "workforce_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceTask {

    @Id
    private String id;

    private String title;
    private String description;
    private String taskType; // "ORDER_PACKING", "INVENTORY_CHECK", "CLEANING", "GENERAL"

    private String referenceOrderId; // optional link to an order

    @Indexed
    private String assignedToUserId;
    @Indexed
    private String assignedByUserId;

    @Builder.Default
    private String status = "OPEN"; // OPEN, IN_PROGRESS, DONE, CANCELLED

    private Instant dueAt;
    private Instant completedAt;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
