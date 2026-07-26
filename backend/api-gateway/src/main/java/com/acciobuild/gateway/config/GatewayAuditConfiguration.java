package com.acciobuild.gateway.config;

import com.acciobuild.gateway.domain.event.GatewayDomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Audit configuration logging secure gateway completed, slow, or security violation events.
 */
@Configuration
@Slf4j
public class GatewayAuditConfiguration {

    @EventListener
    public void auditLogGatewayEvent(GatewayDomainEvent event) {
        log.info("[GATEWAY AUDIT] Type: {} | TenantId: {} | CorrelationId: {} | Timestamp: {}",
                event.getEventType(), event.getOrganizationId(), event.getCorrelationId(), event.getTimestamp());
    }
}
