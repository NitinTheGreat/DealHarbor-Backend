package com.dealharbor.dealharbor_backend.enums;

public enum NotificationType {
    ORDER_CREATED("Order Created", "New order received"),
    ORDER_CONFIRMED("Order Confirmed", "Order has been confirmed"),
    ORDER_COMPLETED("Order Completed", "Order has been completed"),
    ORDER_CANCELLED("Order Cancelled", "Order has been cancelled"),
    
    PRODUCT_APPROVED("Product Approved", "Your product has been approved"),
    PRODUCT_REJECTED("Product Rejected", "Your product has been rejected"),
    PRODUCT_SOLD("Product Sold", "Your product has been sold"),
    PRODUCT_UPDATE("Product Update", "Update on your product listing"),
    
    NEW_MESSAGE("New Message", "You have a new message"),
    NEW_REVIEW("New Review", "You have received a new review"),
    
    PRICE_DROP("Price Drop", "Price dropped on a product you're watching"),
    BACK_IN_STOCK("Back in Stock", "A product you're watching is back in stock"),
    
    ACCOUNT_VERIFIED("Account Verified", "Your account has been verified"),
    SECURITY_ALERT("Security Alert", "Security-related notification"),
    
    SYSTEM_ANNOUNCEMENT("System Announcement", "Important system announcement");

    private final String displayName;
    private final String description;

    NotificationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
