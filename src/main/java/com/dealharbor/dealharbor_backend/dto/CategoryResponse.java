package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class CategoryResponse {
    private String id;
    private String name;
    private String description;
    private String parentId;
    private String iconUrl;
    private boolean isActive;
    private Integer sortOrder;
    private Instant createdAt;
    private Long productCount;
    private List<CategoryResponse> subcategories;
}
