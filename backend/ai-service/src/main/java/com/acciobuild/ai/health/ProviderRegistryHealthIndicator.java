package com.acciobuild.ai.health;

import com.acciobuild.ai.provider.AiProviderRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator tracking active LLM adapter registration status in AiProviderRegistry.
 */
@Component("providerRegistryCustom")
public class ProviderRegistryHealthIndicator implements HealthIndicator {
    private final AiProviderRegistry registry;

    public ProviderRegistryHealthIndicator(AiProviderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Health health() {
        int activeCount = registry.getActiveProviders().size();
        if (activeCount > 0) {
            return Health.up().withDetail("activeProviders", activeCount).build();
        }
        return Health.down().withDetail("error", "No active LLM providers registered").build();
    }
}
