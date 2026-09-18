package com.example.TaskAPI.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {BaseEntityRequestValidator.class, BaseExtensionEntityRequestValidator.class})
public @interface ValidEntityRequest {
    String message() default "Invalid request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
