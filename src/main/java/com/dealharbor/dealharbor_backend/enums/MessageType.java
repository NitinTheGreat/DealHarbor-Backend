package com.dealharbor.dealharbor_backend.enums;

public enum MessageType {
    TEXT("Text", "Regular text message"),
    IMAGE("Image", "Image attachment"),
    FILE("File", "File attachment"),
    SYSTEM("System", "System generated message"),
    ORDER_UPDATE("Order Update", "Order status update message");

    private final String displayName;
    private final String description;

    MessageType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
