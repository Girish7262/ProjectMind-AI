package com.acciobuild.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation validating whether a string fits standard UUID v4 format.
 */
@Documented
@Constraint(validatedBy = UuidValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UuidConstraint {
    String message() default "Invalid UUID formatting format.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
