package com.qavzuro.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    private String url;
    private String altText;
    @Builder.Default
    private int sortOrder = 0;
    @Builder.Default
    private boolean primary = false;
}
