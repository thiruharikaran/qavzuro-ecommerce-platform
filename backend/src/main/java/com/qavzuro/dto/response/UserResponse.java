package com.qavzuro.dto.response;

import com.qavzuro.domain.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Set<String> roleCodes;
    private List<Address> addresses;
    private boolean enabled;
    private Instant createdAt;
}
