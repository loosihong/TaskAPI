package com.example.TaskAPI.core.validation;

import com.example.TaskAPI.core.dto.BaseEntityDetailRequest;
import com.example.TaskAPI.core.model.BaseEntity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BaseEntityRequestValidator implements ConstraintValidator<ValidEntityRequest, BaseEntityDetailRequest> {
    @Override
    public boolean isValid(BaseEntityDetailRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (value.version() == null) {
            context.buildConstraintViolationWithTemplate(BaseEntity.Fields.version + " is required")
                    .addPropertyNode(BaseEntity.Fields.version)
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
