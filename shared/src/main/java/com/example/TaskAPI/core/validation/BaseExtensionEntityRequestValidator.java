package com.example.TaskAPI.core.validation;

import com.example.TaskAPI.core.dto.BaseExtensionEntityDetailRequest;
import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.core.model.BaseExtensionEntity;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BaseExtensionEntityRequestValidator
        implements ConstraintValidator<ValidEntityRequest, BaseExtensionEntityDetailRequest> {
    @Override
    public boolean isValid(BaseExtensionEntityDetailRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (value.version() == null) {
            context.buildConstraintViolationWithTemplate(BaseExtensionEntity.Fields.version + " is required")
                    .addPropertyNode(BaseEntity.Fields.version)
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
