package com.qavzuro.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequest {
    @Min(0) private int quantity; // 0 = remove
}
