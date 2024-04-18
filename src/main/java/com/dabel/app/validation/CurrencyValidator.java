package com.dabel.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class CurrencyValidator implements ConstraintValidator<Currency, String> {
    @Override
    public boolean isValid(String currency, ConstraintValidatorContext constraintValidatorContext) {
        return Arrays.stream(com.dabel.constant.Currency.values())
                .anyMatch(c -> c.name().equals(currency));
    }
}