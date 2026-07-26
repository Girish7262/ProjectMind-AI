package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when handler routes exceed acceptable response thresholds.
 */
@Getter
public class SlowRequestDetectedEvent extends AiDomainEvent {
    private final String requestUri;
    private final String httpMethod;
    private final long durationMs;

    public SlowRequestDetectedEvent(UUID organizationId, String requestUri, String httpMethod, long durationMs, String correlationId) {
        super("SLOW_REQUEST_DETECTED", organizationId, correlationId);
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.durationMs = durationMs;
    }
}
