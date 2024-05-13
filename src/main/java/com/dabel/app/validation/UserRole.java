package com.dabel.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserRoleValidator.class)
public @interface UserRole {
    String message() default "Unknown user role";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
