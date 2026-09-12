package com.qavzuro.service;

import com.qavzuro.domain.Role;
import com.qavzuro.domain.User;
import com.qavzuro.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves a user's effective permissions as the union of all permissions
 * granted by all of their assigned roles. MASTER_ADMIN_ALL is a wildcard
 * that grants every permission - used only by the Master Admin role.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final RoleRepository roleRepository;

    public Set<String> resolvePermissions(User user) {
        List<Role> roles = roleRepository.findByCodeIn(List.copyOf(user.getRoleCodes()));
        Set<String> permissions = new HashSet<>();
        for (Role role : roles) {
            permissions.addAll(role.getPermissionCodes());
        }
        if (permissions.contains(PermissionCodes.MASTER_ADMIN_ALL)) {
            permissions.addAll(PermissionCodes.all());
        }
        return permissions;
    }
}
