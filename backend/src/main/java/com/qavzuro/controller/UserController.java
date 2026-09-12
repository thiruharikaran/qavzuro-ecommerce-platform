package com.qavzuro.controller;

import com.qavzuro.dto.request.AddressRequest;
import com.qavzuro.dto.request.ChangePasswordRequest;
import com.qavzuro.dto.request.UpdateProfileRequest;
import com.qavzuro.dto.response.UserResponse;
import com.qavzuro.mapper.UserMapper;
import com.qavzuro.service.CurrentUserService;
import com.qavzuro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(UserMapper.toResponse(userService.getById(currentUserService.getUserId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.updateProfile(currentUserService.getUserId(), req)));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(currentUserService.getUserId(), req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<com.qavzuro.domain.Address>> myAddresses() {
        return ResponseEntity.ok(userService.getById(currentUserService.getUserId()).getAddresses());
    }

    @PutMapping("/me/addresses")
    public ResponseEntity<List<com.qavzuro.domain.Address>> upsertAddress(@Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(userService.upsertAddress(currentUserService.getUserId(), req));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<List<com.qavzuro.domain.Address>> deleteAddress(@PathVariable String addressId) {
        return ResponseEntity.ok(userService.deleteAddress(currentUserService.getUserId(), addressId));
    }

    // ---- Admin/staff user management (permission-checked) ----

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<org.springframework.data.domain.Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var result = userService.list(org.springframework.data.domain.PageRequest.of(page, size))
                .map(UserMapper::toResponse);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.getById(id)));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<UserResponse> assignRoles(@PathVariable String id,
                                                      @Valid @RequestBody com.qavzuro.dto.request.AssignRoleRequest req) {
        return ResponseEntity.ok(UserMapper.toResponse(
                userService.assignRoles(currentUserService.getUserId(), id, req.getRoleCodes())));
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserResponse> setEnabled(@PathVariable String id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(UserMapper.toResponse(
                userService.setEnabled(currentUserService.getUserId(), id, enabled)));
    }
}
