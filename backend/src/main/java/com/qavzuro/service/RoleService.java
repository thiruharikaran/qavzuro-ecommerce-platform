package com.qavzuro.service;

import com.qavzuro.domain.Role;
import com.qavzuro.dto.request.CreateRoleRequest;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ConflictException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.PermissionRepository;
import com.qavzuro.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    public Role createRole(CreateRoleRequest req) {
        if (roleRepository.existsByCode(req.getCode())) {
            throw new ConflictException("A role with this code already exists.");
        }
        Set<String> perms = validatePermissionCodes(req.getPermissionCodes());
        Role role = Role.builder()
                .code(req.getCode().toUpperCase())
                .name(req.getName())
                .description(req.getDescription())
                .permissionCodes(perms)
                .system(false)
                .build();
        return roleRepository.save(role);
    }

    public Role updatePermissions(String roleId, Set<String> permissionCodes) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
        role.setPermissionCodes(validatePermissionCodes(permissionCodes));
        return roleRepository.save(role);
    }

    public void deleteRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
        if (role.isSystem()) {
            throw new BadRequestException("System roles cannot be deleted.");
        }
        roleRepository.deleteById(roleId);
    }

    private Set<String> validatePermissionCodes(Set<String> codes) {
        if (codes == null) return new HashSet<>();
        for (String code : codes) {
            if (!permissionRepository.existsByCode(code)) {
                throw new BadRequestException("Unknown permission code: " + code);
            }
        }
        return new HashSet<>(codes);
    }
}
