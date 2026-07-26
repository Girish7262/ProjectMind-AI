package com.acciobuild.ai.provider.strategy;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.AiModelCapability;
import com.acciobuild.ai.provider.AiProvider;
import java.util.List;

/**
 * Strategy supporting automated failover routing when primary providers fail.
 */
public class FallbackSelectionStrategy implements AiProviderStrategy {
    private final ProviderType primaryType;
    private final ProviderType fallbackType;

    public FallbackSelectionStrategy(ProviderType primaryType, ProviderType fallbackType) {
        this.primaryType = primaryType;
        this.fallbackType = fallbackType;
    }

    @Override
    public AiProvider selectProvider(List<AiProvider> availableProviders, AiModelCapability requiredCapability) {
        AiProvider primary = availableProviders.stream()
                .filter(p -> p.getType() == primaryType && p.isHealthy() && (requiredCapability == null || p.supportsCapability(requiredCapability)))
                .findFirst()
                .orElse(null);

        if (primary != null) {
            return primary;
        }

        return availableProviders.stream()
                .filter(p -> p.getType() == fallbackType && p.isHealthy() && (requiredCapability == null || p.supportsCapability(requiredCapability)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Fallback selection failed: neither primary nor fallback providers are available."));
    }
}
