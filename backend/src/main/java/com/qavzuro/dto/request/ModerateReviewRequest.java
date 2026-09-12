package com.qavzuro.dto.request;

import com.qavzuro.domain.ReviewModerationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModerateReviewRequest {
    @NotNull private ReviewModerationStatus status;
}
