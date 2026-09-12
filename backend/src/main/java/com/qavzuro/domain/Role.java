package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * A named bundle of permission codes. Users are assigned one or more roles;
 * effective permissions are the union of all their roles' permissions.
 * Roles can be created/edited without redeploying (data-driven authorization).
 */
@Document(collection = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // e.g. "MASTER_ADMIN", "ADMIN", "MANAGER", "TEAM_LEAD", "SUPERVISOR", "WORKER", "CLEANER", "CUSTOMER"

    private String name;
    private String description;

    @Builder.Default
    private Set<String> permissionCodes = new HashSet<>();

    /** System roles cannot be deleted through the admin API. */
    @Builder.Default
    private boolean system = false;
}
