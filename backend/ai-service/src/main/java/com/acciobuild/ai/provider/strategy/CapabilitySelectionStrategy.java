package com.acciobuild.ai.provider.strategy;

import com.acciobuild.ai.provider.AiModelCapability;
import com.acciobuild.ai.provider.AiProvider;
import java.util.List;

/**
 * Strategy selecting the first provider matching explicit capability constraints.
 */
public class CapabilitySelectionStrategy implements AiProviderStrategy {
    
    @Override
    public AiProvider selectProvider(List<AiProvider> availableProviders, AiModelCapability requiredCapability) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No active AI providers available.");
        }
        if (requiredCapability == null) {
            return availableProviders.get(0);
        }
        return availableProviders.stream()
                .filter(p -> p.supportsCapability(requiredCapability))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active provider supports capability: " + requiredCapability));
    }
}
