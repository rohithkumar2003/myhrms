package com.example.demo.model;

public enum LeaveStatus {
    PENDING, APPROVED, REJECTED;
    
    public static LeaveStatus fromString(String value) {
        if (value == null) return null;
        try {
            return LeaveStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}