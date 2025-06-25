package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.CategoryResponse;
import com.dealharbor.dealharbor_backend.entities.Category;
import com.dealharbor.dealharbor_backend.repositories.CategoryRepository;
import com.dealharbor.dealharbor_backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getMainCategories() {
        List<Category> mainCategories = categoryRepository.findMainCategories();
        return mainCategories.stream()
                .map(category -> {
                    CategoryResponse response = convertToCategoryResponse(category);
                    // Load subcategories
                    List<Category> subcategories = categoryRepository.findSubCategories(category.getId());
                    response.setSubcategories(subcategories.stream()
                            .map(this::convertToCategoryResponse)
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getSubCategories(String parentId) {
        List<Category> subcategories = categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(parentId);
        return subcategories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return convertToCategoryResponse(category);
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        long productCount = productRepository.countByCategoryIdAndStatus(
                category.getId(), 
                com.dealharbor.dealharbor_backend.enums.ProductStatus.APPROVED
        );
        
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                category.getIconUrl(),
                category.getIsActive(),
                category.getSortOrder(),
                category.getCreatedAt(),
                productCount,
                null // Subcategories loaded separately when needed
        );
    }
}
