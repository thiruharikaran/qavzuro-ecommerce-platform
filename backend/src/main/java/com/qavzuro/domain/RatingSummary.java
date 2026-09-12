package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingSummary {
    @Builder.Default
    private double average = 0.0;
    @Builder.Default
    private long count = 0;
}
