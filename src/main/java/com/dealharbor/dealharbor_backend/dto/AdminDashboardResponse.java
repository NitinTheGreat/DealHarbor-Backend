package com.dealharbor.dealharbor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardResponse {
    // User Statistics
    private long totalUsers;
    private long activeUsers;
    private long bannedUsers;
    private long verifiedStudents;
    
    // Product Statistics
    private long totalProducts;
    private long pendingProducts;
    private long approvedProducts;
    private long soldProducts;
    private long featuredProducts;
    
    // Order Statistics
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
    
    // Review Statistics
    private long totalReviews;
    private long pendingReviews;
    
    // Recent Activity
    private long todayRegistrations;
    private long todayProducts;
    private long todayOrders;
}
