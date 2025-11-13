package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProductPreview {
    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryImage;
    private int totalProducts;
    private List<ProductResponse> products;
}
