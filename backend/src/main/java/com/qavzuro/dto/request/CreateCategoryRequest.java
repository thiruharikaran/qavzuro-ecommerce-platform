package com.qavzuro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank private String name;
    @NotBlank private String slug;
    private String description;
    private String imageUrl;
    private String parentId;
}
