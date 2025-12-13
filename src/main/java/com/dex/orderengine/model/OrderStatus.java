package com.dex.orderengine.model;

public enum OrderStatus {
    PENDING("pending"),
    ROUTING("routing"),
    BUILDING("building"),
    SUBMITTED("submitted"),
    CONFIRMED("confirmed"),
    FAILED("failed");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
