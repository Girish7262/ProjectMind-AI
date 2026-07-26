package com.acciobuild.auth.security.event;

import com.acciobuild.auth.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener capturing SecurityEvent publications.
 * Persists records to database asynchronously off the main thread.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEventListener {

    private final AuditEventService auditEventService;

    /**
     * Handles published SecurityEvents asynchronously to avoid blocking the caller's request thread.
     */
    @Async
    @EventListener
    public void handleSecurityEvent(SecurityEvent event) {
        log.debug("Asynchronously handling security event: {}", event.getAuditEvent().getEventType());
        try {
            auditEventService.saveEvent(event.getAuditEvent());
        } catch (Exception e) {
            log.error("Failed to asynchronously save audit event: {}", e.getMessage(), e);
        }
    }
}
