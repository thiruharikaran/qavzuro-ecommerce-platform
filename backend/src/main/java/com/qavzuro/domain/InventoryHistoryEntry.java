package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Immutable audit trail of every stock change for a product/variant. */
@Document(collection = "inventory_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryEntry {

    @Id
    private String id;

    @Indexed
    private String productId;
    private String variantId;

    private int quantityChange; // positive = added, negative = removed
    private int quantityAfter;

    private String reason; // "ORDER_PLACED", "ORDER_CANCELLED", "MANUAL_ADJUSTMENT", "RETURN_RESTOCK"
    private String reference; // order id, etc.
    private String actorUserId;

    @CreatedDate
    private Instant createdAt;
}
