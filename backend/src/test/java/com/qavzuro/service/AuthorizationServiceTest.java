package com.qavzuro.service;

import com.qavzuro.domain.Role;
import com.qavzuro.domain.User;
import com.qavzuro.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Covers the data-driven Role -> Permission -> User resolution (no hardcoded role checks). */
class AuthorizationServiceTest {

    @Test
    void resolvesUnionOfPermissionsAcrossAllUserRoles() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        Role supervisor = Role.builder().code("SUPERVISOR").permissionCodes(Set.of("ORDER_VIEW", "INVENTORY_VIEW")).build();
        Role reviewer = Role.builder().code("REVIEWER").permissionCodes(Set.of("REVIEW_MODERATE")).build();
        when(roleRepository.findByCodeIn(any())).thenReturn(List.of(supervisor, reviewer));

        User user = User.builder().roleCodes(new HashSet<>(Set.of("SUPERVISOR", "REVIEWER"))).build();

        AuthorizationService service = new AuthorizationService(roleRepository);
        Set<String> permissions = service.resolvePermissions(user);

        assertEquals(Set.of("ORDER_VIEW", "INVENTORY_VIEW", "REVIEW_MODERATE"), permissions);
    }

    @Test
    void masterAdminWildcard_grantsEveryPermission() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        Role masterAdmin = Role.builder().code("MASTER_ADMIN").permissionCodes(Set.of(PermissionCodes.MASTER_ADMIN_ALL)).build();
        when(roleRepository.findByCodeIn(any())).thenReturn(List.of(masterAdmin));

        User user = User.builder().roleCodes(new HashSet<>(Set.of("MASTER_ADMIN"))).build();

        AuthorizationService service = new AuthorizationService(roleRepository);
        Set<String> permissions = service.resolvePermissions(user);

        assertTrue(permissions.containsAll(PermissionCodes.all()), "Master Admin must effectively have every permission");
    }

    @Test
    void userWithNoRoles_hasNoPermissions() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        when(roleRepository.findByCodeIn(any())).thenReturn(List.of());

        User user = User.builder().roleCodes(new HashSet<>()).build();

        AuthorizationService service = new AuthorizationService(roleRepository);
        assertTrue(service.resolvePermissions(user).isEmpty());
    }
}
