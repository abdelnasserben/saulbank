package com.dabel.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class CardTypeValidator implements ConstraintValidator<CardType, String> {
    @Override
    public boolean isValid(String cardType, ConstraintValidatorContext constraintValidatorContext) {
        return Arrays.stream(com.dabel.constant.CardType.values())
                .anyMatch(c -> c.name().equals(cardType));
    }
}