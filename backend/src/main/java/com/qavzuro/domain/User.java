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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;

    /** Role codes assigned to this user, e.g. {"CUSTOMER"} or {"WORKER","SUPERVISOR"}. */
    @Builder.Default
    private Set<String> roleCodes = new HashSet<>();

    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountLocked = false;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;

    /** Workforce-only: which supervisor/team-lead this user reports to. */
    private String reportsToUserId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
