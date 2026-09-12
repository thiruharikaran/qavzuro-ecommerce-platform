package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRoleRequest {
    @NotEmpty private Set<String> roleCodes;
}
