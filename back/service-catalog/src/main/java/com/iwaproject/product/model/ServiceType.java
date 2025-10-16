package com.iwaproject.product.model;

public enum ServiceType {
    BOOSTING("Boosting"),
    COACHING("Coaching"),
    ACCOUNT_RESALING("Account resaling"),
    OTHER("Other");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

