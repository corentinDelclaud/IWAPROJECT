package com.iwaproject.product.model;

public enum ServiceType {
    BOOST("BOOST"),
    COACHING("COACHING"),
    ACCOUNT_RESALING("ACCOUNT RESALING"),
    OTHER("OTHER");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

