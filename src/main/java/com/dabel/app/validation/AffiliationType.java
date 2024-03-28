package com.dabel.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AffiliationTypeValidator.class)
public @interface AffiliationType {
    String message() default "Affiliation type must be Add or Remove";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
