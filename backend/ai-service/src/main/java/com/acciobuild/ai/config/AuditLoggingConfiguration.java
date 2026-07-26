package com.acciobuild.ai.config;

import com.acciobuild.ai.domain.event.AiDomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Audit logging configuration listening to security, memory, prompt, and tool events.
 */
@Configuration
@Slf4j
public class AuditLoggingConfiguration {

    @EventListener
    public void auditLogDomainEvent(AiDomainEvent event) {
        log.info("[SECURE AUDIT LOG] EventType: {}, TenantId: {}, CorrelationId: {}, OccurredAt: {}",
                event.getEventType(), event.getOrganizationId(), event.getCorrelationId(), event.getTimestamp());
    }
}
