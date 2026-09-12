package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {
    private String productId;
    private String productNameSnapshot;
    private String imageUrlSnapshot;
    private Instant addedAt;
}
