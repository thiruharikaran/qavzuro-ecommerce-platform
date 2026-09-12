package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateWorkforceTaskStatusRequest {
    @NotBlank private String status; // OPEN, IN_PROGRESS, DONE, CANCELLED
}
