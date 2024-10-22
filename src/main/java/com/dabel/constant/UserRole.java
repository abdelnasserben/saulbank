package com.dabel.constant;

public enum UserRole {
    CASHIER ("Best for handling day-to-day transactions and customer payments"),
    RECEPTIONIST("Best for managing customer inquiries and front-desk interactions"),
    LOANER("Best for processing and managing customer loan applications"),
    BO("Best for overseeing administrative tasks and backend operations"),
    MANGER("Best for supervising teams and overseeing branch or departmental operations"),
    ADMIN("Best for managing system settings and overseeing all user roles and privileges");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
