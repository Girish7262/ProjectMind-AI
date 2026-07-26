package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * OpenAI stub adapter implementation.
 */
@Component
public class OpenAiProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.OPENAI;
    }

    @Override
    public String getName() {
        return "OpenAI";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[OpenAI Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(20)
                .outputTokens(40)
                .estimatedCost(0.00012)
                .latencyMs(latency)
                .provider(getName())
                .model("gpt-4o")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of("flagged", false))
                .latencyMs(latency)
                .providerMetadata(Map.of("api_status", "healthy"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[OpenAI Stream Chunk 1] ");
        handler.onChunk("[OpenAI Stream Chunk 2] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return true;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo");
    }

    @Override
    public int getDefaultPriority() {
        return 100;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
