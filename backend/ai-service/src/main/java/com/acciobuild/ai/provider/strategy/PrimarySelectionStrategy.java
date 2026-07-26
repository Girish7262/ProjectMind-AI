package com.acciobuild.ai.provider.strategy;

import com.acciobuild.ai.provider.AiModelCapability;
import com.acciobuild.ai.provider.AiProvider;
import java.util.List;

/**
 * Strategy selecting the primary active provider from the load list.
 */
public class PrimarySelectionStrategy implements AiProviderStrategy {
    
    @Override
    public AiProvider selectProvider(List<AiProvider> availableProviders, AiModelCapability requiredCapability) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No active AI providers available.");
        }
        if (requiredCapability != null) {
            return availableProviders.stream()
                    .filter(p -> p.supportsCapability(requiredCapability))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No provider matches the capability: " + requiredCapability));
        }
        return availableProviders.get(0);
    }
}
