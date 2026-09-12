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

/**
 * Refresh tokens are stored server-side (hashed) so they can be individually
 * revoked and rotated. A refresh token is single-use: on refresh, it is
 * revoked and replaced by a new one (rotation), and if a revoked token is
 * ever presented again, the whole token family is revoked (reuse detection).
 */
@Document(collection = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed
    private String userId;

    /** All tokens descended from the same original login share a family id. */
    @Indexed
    private String familyId;

    private Instant expiresAt;

    @Builder.Default
    private boolean revoked = false;

    private String replacedByTokenHash;
    private String userAgent;
    private String ipAddress;

    @CreatedDate
    private Instant createdAt;
}
