package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini stub adapter implementation.
 */
@Component
public class GeminiProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.GEMINI;
    }

    @Override
    public String getName() {
        return "Google Gemini";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[Google Gemini Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(18)
                .outputTokens(35)
                .estimatedCost(0.00007)
                .latencyMs(latency)
                .provider(getName())
                .model("gemini-1.5-pro")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of("harmless", true))
                .latencyMs(latency)
                .providerMetadata(Map.of("gemini_safety_ratings", "block_none"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[Gemini Chunk] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return capability != AiModelCapability.EMBEDDING;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("gemini-1.5-pro", "gemini-1.5-flash");
    }

    @Override
    public int getDefaultPriority() {
        return 85;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
