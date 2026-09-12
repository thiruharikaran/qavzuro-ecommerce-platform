package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A single, fine-grained capability (e.g. PRODUCT_CREATE, ORDER_CANCEL).
 * Permissions are the atomic unit of authorization; roles are just named
 * bundles of permissions. New capabilities can be added here without any
 * change to authorization logic elsewhere.
 */
@Document(collection = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // e.g. "PRODUCT_CREATE"

    private String description;
    private String category; // e.g. "PRODUCT", "ORDER", "WORKFORCE" - for grouping in admin UI
}
