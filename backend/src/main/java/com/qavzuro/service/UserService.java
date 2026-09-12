package com.qavzuro.service;

import com.qavzuro.domain.Address;
import com.qavzuro.domain.User;
import com.qavzuro.dto.request.AddressRequest;
import com.qavzuro.dto.request.ChangePasswordRequest;
import com.qavzuro.dto.request.UpdateProfileRequest;
import com.qavzuro.exception.AuthenticationFailedException;
import com.qavzuro.exception.BadRequestException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public User getById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateProfile(String userId, UpdateProfileRequest req) {
        User user = getById(userId);
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        return userRepository.save(user);
    }

    public void changePassword(String userId, ChangePasswordRequest req) {
        User user = getById(userId);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        auditService.record(userId, user.getEmail(), "PASSWORD_CHANGED", "USER", userId, null);
    }

    public List<Address> upsertAddress(String userId, AddressRequest req) {
        User user = getById(userId);
        Address address = Address.builder()
                .id(req.getId() != null ? req.getId() : UUID.randomUUID().toString())
                .label(req.getLabel())
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .line1(req.getLine1())
                .line2(req.getLine2())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .country(req.getCountry())
                .defaultShipping(req.isDefaultShipping())
                .defaultBilling(req.isDefaultBilling())
                .build();

        user.getAddresses().removeIf(a -> a.getId().equals(address.getId()));

        if (address.isDefaultShipping()) {
            user.getAddresses().forEach(a -> a.setDefaultShipping(false));
        }
        if (address.isDefaultBilling()) {
            user.getAddresses().forEach(a -> a.setDefaultBilling(false));
        }

        user.getAddresses().add(address);
        userRepository.save(user);
        return user.getAddresses();
    }

    public List<Address> deleteAddress(String userId, String addressId) {
        User user = getById(userId);
        boolean removed = user.getAddresses().removeIf(a -> a.getId().equals(addressId));
        if (!removed) throw new ResourceNotFoundException("Address not found.");
        userRepository.save(user);
        return user.getAddresses();
    }

    public Address findAddressOrThrow(User user, String addressId) {
        return user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Address not found for this user."));
    }

    public User assignRoles(String actorUserId, String targetUserId, java.util.Set<String> roleCodes) {
        User user = getById(targetUserId);
        user.setRoleCodes(roleCodes);
        User saved = userRepository.save(user);
        auditService.record(actorUserId, null, "ROLE_CHANGED", "USER", targetUserId,
                java.util.Map.of("newRoles", roleCodes));
        return saved;
    }

    public User setEnabled(String actorUserId, String targetUserId, boolean enabled) {
        User user = getById(targetUserId);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditService.record(actorUserId, null, enabled ? "USER_ENABLED" : "USER_DISABLED", "USER", targetUserId, null);
        return saved;
    }
}
