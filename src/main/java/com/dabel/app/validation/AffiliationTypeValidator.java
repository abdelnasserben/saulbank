package com.dabel.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class AffiliationTypeValidator implements ConstraintValidator<AffiliationType, String> {
    @Override
    public boolean isValid(String affiliationType, ConstraintValidatorContext constraintValidatorContext) {
        return List.of("ADD", "REMOVE").contains(affiliationType);
    }
}