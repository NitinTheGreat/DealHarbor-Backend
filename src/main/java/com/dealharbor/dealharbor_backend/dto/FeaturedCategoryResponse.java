package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedCategoryResponse {
    private String id;
    private String name;
    private long productCount;
    private String iconName;
    private String imageUrl;
}
