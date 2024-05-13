package com.dabel.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class UserRoleValidator implements ConstraintValidator<UserRole, String> {
    @Override
    public boolean isValid(String userRole, ConstraintValidatorContext constraintValidatorContext) {
        return Arrays.stream(com.dabel.constant.UserRole.values())
                .anyMatch(role -> role.name().equals(userRole));
    }
}
