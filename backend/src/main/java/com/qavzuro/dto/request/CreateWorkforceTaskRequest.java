package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateWorkforceTaskRequest {
    @NotBlank private String title;
    private String description;
    @NotBlank private String taskType;
    private String referenceOrderId;
    @NotBlank private String assignedToUserId;
    private Instant dueAt;
}
