package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude stub adapter implementation.
 */
@Component
public class AnthropicProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.ANTHROPIC;
    }

    @Override
    public String getName() {
        return "Anthropic Claude";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[Anthropic Claude Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(30)
                .outputTokens(70)
                .estimatedCost(0.00035) // Claude 3 Opus is expensive
                .latencyMs(latency)
                .provider(getName())
                .model("claude-3-opus")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of("safety_evaluation", "approved"))
                .latencyMs(latency)
                .providerMetadata(Map.of("anthropic_version", "2023-06-01"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[Claude Chunk] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return true;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("claude-3-opus", "claude-3-sonnet", "claude-3-haiku");
    }

    @Override
    public int getDefaultPriority() {
        return 95;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
