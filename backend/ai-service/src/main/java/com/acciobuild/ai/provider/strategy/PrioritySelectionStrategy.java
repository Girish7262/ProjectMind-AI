package com.acciobuild.ai.provider.strategy;

import com.acciobuild.ai.provider.AiModelCapability;
import com.acciobuild.ai.provider.AiProvider;
import java.util.List;

/**
 * Strategy selecting the highest-priority active provider matching required capability constraints.
 */
public class PrioritySelectionStrategy implements AiProviderStrategy {
    
    @Override
    public AiProvider selectProvider(List<AiProvider> availableProviders, AiModelCapability requiredCapability) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No active AI providers available.");
        }
        return availableProviders.stream()
                .filter(p -> requiredCapability == null || p.supportsCapability(requiredCapability))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No provider matches the priority selection capability: " + requiredCapability));
    }
}
