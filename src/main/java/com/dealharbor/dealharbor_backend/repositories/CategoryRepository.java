package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(String parentId);
    Optional<Category> findByNameAndIsActiveTrue(String name);
    
    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findMainCategories();
    
    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId AND c.isActive = true ORDER BY c.sortOrder ASC")
    List<Category> findSubCategories(String parentId);
}
