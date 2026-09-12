package com.qavzuro.controller;

import com.qavzuro.domain.Role;
import com.qavzuro.dto.request.CreateRoleRequest;
import com.qavzuro.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<List<Role>> list() {
        return ResponseEntity.ok(roleService.listRoles());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> create(@Valid @RequestBody CreateRoleRequest req) {
        return ResponseEntity.ok(roleService.createRole(req));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Role> updatePermissions(@PathVariable String id, @RequestBody Set<String> permissionCodes) {
        return ResponseEntity.ok(roleService.updatePermissions(id, permissionCodes));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
