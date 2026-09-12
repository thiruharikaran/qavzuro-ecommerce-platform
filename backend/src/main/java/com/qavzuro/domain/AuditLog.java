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
import java.util.HashMap;
import java.util.Map;

@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String actorUserId;
    private String actorEmail;

    @Indexed
    private String action; // "LOGIN", "LOGIN_FAILED", "ROLE_CHANGED", "PRODUCT_UPDATED", ...

    private String resourceType; // "ORDER", "PRODUCT", "USER", ...
    @Indexed
    private String resourceId;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    private String ipAddress;

    @CreatedDate
    private Instant createdAt;
}
