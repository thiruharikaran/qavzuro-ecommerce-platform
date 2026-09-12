package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateReturnRequest {
    @NotBlank private String orderId;
    private List<String> orderItemProductIds;
    @NotBlank private String reason;
    private String customerNote;
}
