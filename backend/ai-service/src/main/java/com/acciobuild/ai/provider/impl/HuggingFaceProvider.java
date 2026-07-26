package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Hugging Face stub adapter implementation.
 */
@Component
public class HuggingFaceProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.HUGGING_FACE;
    }

    @Override
    public String getName() {
        return "Hugging Face";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[Hugging Face Hub Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(12)
                .outputTokens(25)
                .estimatedCost(0.00004)
                .latencyMs(latency)
                .provider(getName())
                .model("tiiuae/falcon-7b")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of())
                .latencyMs(latency)
                .providerMetadata(Map.of("hugging_face_repo", "tiiuae/falcon-7b"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[Hugging Face Chunk] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return capability == AiModelCapability.EMBEDDING || capability == AiModelCapability.CHAT;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("tiiuae/falcon-7b", "meta-llama/Meta-Llama-3-8B");
    }

    @Override
    public int getDefaultPriority() {
        return 40;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
