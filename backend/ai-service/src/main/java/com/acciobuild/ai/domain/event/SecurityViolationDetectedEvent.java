package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when tenant boundaries or auth tokens are violated.
 */
@Getter
public class SecurityViolationDetectedEvent extends AiDomainEvent {
    private final String violationDetails;
    private final String clientIp;

    public SecurityViolationDetectedEvent(UUID organizationId, String violationDetails, String clientIp, String correlationId) {
        super("SECURITY_VIOLATION_DETECTED", organizationId, correlationId);
        this.violationDetails = violationDetails;
        this.clientIp = clientIp;
    }
}
