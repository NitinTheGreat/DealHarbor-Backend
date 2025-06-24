package com.dealharbor.dealharbor_backend.enums;

public enum OrderStatus {
    PENDING("Pending", "Order placed, waiting for seller confirmation"),
    CONFIRMED("Confirmed", "Seller confirmed the order"),
    COMPLETED("Completed", "Order successfully completed"),
    CANCELLED("Cancelled", "Order was cancelled"),
    DISPUTED("Disputed", "Order is under dispute");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
