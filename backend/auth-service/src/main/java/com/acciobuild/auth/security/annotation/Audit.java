package com.acciobuild.auth.security.annotation;

import com.acciobuild.auth.enums.AuditEventType;
import java.lang.annotation.*;

/**
 * Custom annotation to mark methods for automated audit logging using Spring AOP.
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /**
     * Declares the category and type of audit event.
     */
    AuditEventType value();
}
