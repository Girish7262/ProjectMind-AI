package com.acciobuild.ai.provider.strategy;

import com.acciobuild.ai.provider.AiModelCapability;
import com.acciobuild.ai.provider.AiProvider;
import java.util.List;

/**
 * Strategy pattern interface for routing execution requests across active AI providers.
 */
public interface AiProviderStrategy {
    
    /**
     * Chooses the optimal provider based on selection routing rules.
     */
    AiProvider selectProvider(List<AiProvider> availableProviders, AiModelCapability requiredCapability);
}
