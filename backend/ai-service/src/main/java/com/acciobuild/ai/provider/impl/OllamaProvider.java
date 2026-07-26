package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Ollama stub adapter implementation.
 */
@Component
public class OllamaProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.OLLAMA;
    }

    @Override
    public String getName() {
        return "Ollama";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[Ollama Local Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(15)
                .outputTokens(30)
                .estimatedCost(0.0) // Local models cost nothing
                .latencyMs(latency)
                .provider(getName())
                .model("llama3")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of())
                .latencyMs(latency)
                .providerMetadata(Map.of("host", "localhost"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[Ollama Chunk] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return capability == AiModelCapability.CHAT || capability == AiModelCapability.EMBEDDING;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("llama3", "mistral");
    }

    @Override
    public int getDefaultPriority() {
        return 50;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
