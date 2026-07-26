package com.acciobuild.auth.security.event;

import com.acciobuild.auth.entity.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Event publisher managing security audit events distribution.
 * Integrates with Spring ApplicationEventPublisher to support future message bus integrations (e.g., Kafka).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publishes a security event across active application listeners.
     */
    public void publishEvent(AuditEvent event) {
        log.debug("Publishing security audit event: {}", event.getEventType());
        SecurityEvent securityEvent = new SecurityEvent(event);
        applicationEventPublisher.publishEvent(securityEvent);
    }
}
