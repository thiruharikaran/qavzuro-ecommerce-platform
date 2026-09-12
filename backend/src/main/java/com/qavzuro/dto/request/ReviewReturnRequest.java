package com.qavzuro.dto.request;

import com.qavzuro.domain.ReturnStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewReturnRequest {
    @NotNull private ReturnStatus status; // APPROVED, REJECTED, ITEM_RECEIVED, REFUNDED, CLOSED
    private String reviewNote;
    private Double refundAmount;
}
