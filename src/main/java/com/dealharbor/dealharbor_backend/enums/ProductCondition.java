package com.dealharbor.dealharbor_backend.enums;

public enum ProductCondition {
    NEW("New", "Brand new, never used"),
    LIKE_NEW("Like New", "Barely used, excellent condition"),
    GOOD("Good", "Used but in good working condition"),
    FAIR("Fair", "Shows wear but still functional"),
    POOR("Poor", "Heavy wear, may need repairs"),
    USED("Used", "Previously owned, normal wear");

    private final String displayName;
    private final String description;

    ProductCondition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
