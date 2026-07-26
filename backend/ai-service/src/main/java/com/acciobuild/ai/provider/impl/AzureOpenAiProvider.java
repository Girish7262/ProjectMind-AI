package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Azure OpenAI stub adapter implementation.
 */
@Component
public class AzureOpenAiProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.AZURE;
    }

    @Override
    public String getName() {
        return "Azure OpenAI";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[Azure OpenAI Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(25)
                .outputTokens(50)
                .estimatedCost(0.00015)
                .latencyMs(latency)
                .provider(getName())
                .model("azure-gpt-4")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of("filtered", false))
                .latencyMs(latency)
                .providerMetadata(Map.of("azure_deployment", "prod-deploy"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[Azure Stream Chunk] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return capability == AiModelCapability.CHAT || capability == AiModelCapability.JSON_MODE;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("azure-gpt-4", "azure-gpt-35");
    }

    @Override
    public int getDefaultPriority() {
        return 90;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
