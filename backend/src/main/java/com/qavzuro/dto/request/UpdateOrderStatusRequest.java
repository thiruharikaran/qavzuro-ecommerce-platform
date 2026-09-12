package com.qavzuro.dto.request;

import com.qavzuro.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull private OrderStatus status;
    private String note;
    private String trackingNumber;
    private String carrier;
}
