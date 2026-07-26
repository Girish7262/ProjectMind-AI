package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when connection pooling or downstream endpoints respond with high latency.
 */
@Getter
public class HighLatencyDetectedEvent extends AiDomainEvent {
    private final String dependencyName;
    private final long latencyMs;

    public HighLatencyDetectedEvent(UUID organizationId, String dependencyName, long latencyMs, String correlationId) {
        super("HIGH_LATENCY_DETECTED", organizationId, correlationId);
        this.dependencyName = dependencyName;
        this.latencyMs = latencyMs;
    }
}
