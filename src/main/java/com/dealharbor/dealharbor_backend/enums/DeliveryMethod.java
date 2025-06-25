package com.dealharbor.dealharbor_backend.enums;

public enum DeliveryMethod {
    PICKUP("Pickup", "Buyer picks up from seller"),
    DELIVERY("Delivery", "Seller delivers to buyer"),
    SHIPPING("Shipping", "Shipped via courier");

    private final String displayName;
    private final String description;

    DeliveryMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
