package com.dealharbor.dealharbor_backend.enums;

import java.math.BigDecimal;

public enum SellerBadge {
    NEW_SELLER("New Seller", "Just getting started", "#6B7280", 0),
    ACTIVE_SELLER("Active Seller", "Regular seller with good activity", "#3B82F6", 5),
    TRUSTED_SELLER("Trusted Seller", "Reliable seller with great reviews", "#10B981", 15),
    DEALHARBOR_CHOICE("DealHarbor's Choice", "Top-rated seller with excellent track record", "#F59E0B", 30),
    PREMIUM_SELLER("Premium Seller", "Elite seller with outstanding performance", "#8B5CF6", 50),
    LEGENDARY_SELLER("Legendary Seller", "The best of the best", "#EF4444", 100);

    private final String displayName;
    private final String description;
    private final String color; // Hex color for UI
    private final int minimumSales; // Minimum sales required

    SellerBadge(String displayName, String description, String color, int minimumSales) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
        this.minimumSales = minimumSales;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getColor() { return color; }
    public int getMinimumSales() { return minimumSales; }

    // Method to calculate badge based on performance (updated for BigDecimal)
    public static SellerBadge calculateBadge(int totalSales, BigDecimal sellerRating, BigDecimal responseRate, int positiveReviews) {
        double rating = sellerRating.doubleValue();
        double response = responseRate.doubleValue();
        
        if (totalSales >= 100 && rating >= 4.8 && response >= 95 && positiveReviews >= 80) {
            return LEGENDARY_SELLER;
        } else if (totalSales >= 50 && rating >= 4.5 && response >= 90 && positiveReviews >= 40) {
            return PREMIUM_SELLER;
        } else if (totalSales >= 30 && rating >= 4.2 && response >= 85 && positiveReviews >= 25) {
            return DEALHARBOR_CHOICE;
        } else if (totalSales >= 15 && rating >= 4.0 && response >= 80 && positiveReviews >= 12) {
            return TRUSTED_SELLER;
        } else if (totalSales >= 5 && rating >= 3.5) {
            return ACTIVE_SELLER;
        } else {
            return NEW_SELLER;
        }
    }
}
