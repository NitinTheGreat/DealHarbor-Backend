package com.dealharbor.dealharbor_backend.enums;

public enum ReviewType {
    SELLER_REVIEW("Seller Review", "Review for the seller"),
    BUYER_REVIEW("Buyer Review", "Review for the buyer");

    private final String displayName;
    private final String description;

    ReviewType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
