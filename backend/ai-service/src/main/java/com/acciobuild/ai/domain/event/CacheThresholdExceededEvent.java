package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when Redis caching key capacity limits or eviction counts spike.
 */
@Getter
public class CacheThresholdExceededEvent extends AiDomainEvent {
    private final String cacheName;
    private final double usagePercentage;

    public CacheThresholdExceededEvent(UUID organizationId, String cacheName, double usagePercentage, String correlationId) {
        super("CACHE_THRESHOLD_EXCEEDED", organizationId, correlationId);
        this.cacheName = cacheName;
        this.usagePercentage = usagePercentage;
    }
}
