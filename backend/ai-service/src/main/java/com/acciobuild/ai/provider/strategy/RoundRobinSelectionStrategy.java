package com.acciobuild.ai.provider.strategy;

import com.acciobuild.ai.provider.AiModelCapability;
import com.acciobuild.ai.provider.AiProvider;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Strategy distributing routing traffic round-robin style across active backends.
 */
public class RoundRobinSelectionStrategy implements AiProviderStrategy {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public AiProvider selectProvider(List<AiProvider> availableProviders, AiModelCapability requiredCapability) {
        if (availableProviders == null || availableProviders.isEmpty()) {
            throw new IllegalStateException("No active AI providers available.");
        }

        List<AiProvider> filtered = availableProviders;
        if (requiredCapability != null) {
            filtered = availableProviders.stream()
                    .filter(p -> p.supportsCapability(requiredCapability))
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("No provider matches the capability: " + requiredCapability);
        }

        int nextIndex = Math.abs(index.getAndIncrement() % filtered.size());
        return filtered.get(nextIndex);
    }
}
