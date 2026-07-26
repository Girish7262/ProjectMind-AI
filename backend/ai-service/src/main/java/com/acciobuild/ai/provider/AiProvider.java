package com.acciobuild.ai.provider;

import com.acciobuild.ai.enums.ProviderType;
import java.util.List;

/**
 * Common abstraction representing an interchangeable AI model provider execution backend.
 */
public interface AiProvider {
    
    /**
     * Gets the enum type representing this provider.
     */
    ProviderType getType();
    
    /**
     * Gets the readable config name.
     */
    String getName();
    
    /**
     * Executes a synchronous request.
     */
    AiResponse execute(AiRequest request);
    
    /**
     * Executes a streaming request.
     */
    void executeStream(AiRequest request, AiStreamingHandler handler);
    
    /**
     * Verifies if the provider/model supports a specific capability.
     */
    boolean supportsCapability(AiModelCapability capability);
    
    /**
     * Lists discoverable models supported by this backend wrapper.
     */
    List<String> getDiscoverableModels();
    
    /**
     * Gets default load-balancer prioritization rank. Higher value = higher priority.
     */
    int getDefaultPriority();
    
    /**
     * Health status check stub.
     */
    boolean isHealthy();
}
