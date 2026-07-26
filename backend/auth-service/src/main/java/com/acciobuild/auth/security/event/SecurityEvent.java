package com.acciobuild.auth.security.event;

import com.acciobuild.auth.entity.AuditEvent;
import org.springframework.context.ApplicationEvent;

/**
 * Spring ApplicationEvent subclass encapsulating security audit logs details.
 */
public class SecurityEvent extends ApplicationEvent {

    private final AuditEvent auditEvent;

    /**
     * Constructs a new SecurityEvent.
     */
    public SecurityEvent(AuditEvent source) {
        super(source);
        this.auditEvent = source;
    }

    /**
     * Retrieves the enclosed AuditEvent entity model details.
     */
    public AuditEvent getAuditEvent() {
        return this.auditEvent;
    }
}
