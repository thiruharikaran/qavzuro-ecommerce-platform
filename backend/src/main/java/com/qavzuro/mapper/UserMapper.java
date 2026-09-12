package com.qavzuro.mapper;

import com.qavzuro.domain.User;
import com.qavzuro.dto.response.UserResponse;

/** Never exposes passwordHash or other sensitive fields. */
public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .roleCodes(user.getRoleCodes())
                .addresses(user.getAddresses())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
