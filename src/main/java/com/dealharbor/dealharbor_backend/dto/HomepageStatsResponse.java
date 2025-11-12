package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageStatsResponse {
    private long totalProducts;
    private long totalActiveProducts;
    private long totalUsers;
    private long totalVerifiedStudents;
    private long totalSellers;
    private long totalCategories;
    private long productsAddedToday;
    private long productsAddedThisWeek;
    private String mostPopularCategory;
    private long mostPopularCategoryCount;
}
