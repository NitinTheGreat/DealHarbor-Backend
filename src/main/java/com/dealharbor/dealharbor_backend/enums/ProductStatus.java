package com.dealharbor.dealharbor_backend.enums;

public enum ProductStatus {
    PENDING("Pending Review", "Waiting for admin approval"),
    APPROVED("Approved", "Available for purchase"),
    REJECTED("Rejected", "Not approved for listing"),
    SOLD("Sold", "Successfully sold"),
    DELETED("Deleted", "Removed by user or admin");

    private final String displayName;
    private final String description;

    ProductStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
