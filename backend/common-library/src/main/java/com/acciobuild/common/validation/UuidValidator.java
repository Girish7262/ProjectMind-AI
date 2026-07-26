package com.acciobuild.common.validation;

import com.acciobuild.common.util.ValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Constraint validator checking if string fits UUID v4 formats.
 */
public class UuidValidator implements ConstraintValidator<UuidConstraint, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Allow nulls, use @NotNull separately if required
        }
        return ValidationUtils.isValidUuid(value);
    }
}
