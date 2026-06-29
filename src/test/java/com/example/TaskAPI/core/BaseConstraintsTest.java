package com.example.TaskAPI.core;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseConstraintsTest {
    protected static final String HAPPY_CASE_MESSAGE = "Field [{1}] has no error";
    protected static final String VIOLATION_CASE_MESSAGE = "Field [{1}] shows error: {2}";
    protected final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    protected void assertNoViolation(Set<? extends ConstraintViolation<?>> violations, String field) {
        assertThat(violations)
                .noneMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    protected void assertViolation(Set<? extends ConstraintViolation<?>> violations, String field, String error) {
        assertThat(violations).anySatisfy(v -> {
            assertThat(v.getPropertyPath().toString()).isEqualTo(field);
            assertThat(v.getMessage()).isEqualTo(error);
        });
    }
}