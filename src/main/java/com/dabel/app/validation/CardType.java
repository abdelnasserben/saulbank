package com.dabel.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CardTypeValidator.class)
public @interface CardType {
    String message() default "Card type must be Visa or Mastercard";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
