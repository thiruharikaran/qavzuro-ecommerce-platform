package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String description;
    private Set<String> permissionCodes;
}
