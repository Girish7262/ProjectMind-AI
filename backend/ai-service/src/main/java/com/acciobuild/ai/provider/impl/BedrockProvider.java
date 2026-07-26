package com.acciobuild.ai.provider.impl;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AWS Bedrock stub adapter implementation.
 */
@Component
public class BedrockProvider implements AiProvider {

    @Override
    public ProviderType getType() {
        return ProviderType.BEDROCK;
    }

    @Override
    public String getName() {
        return "AWS Bedrock";
    }

    @Override
    public AiResponse execute(AiRequest request) {
        long startTime = System.currentTimeMillis();
        String generated = "[AWS Bedrock Stub Response] for prompt: " + request.getPrompt();
        long latency = System.currentTimeMillis() - startTime;
        
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(30)
                .outputTokens(60)
                .estimatedCost(0.00025)
                .latencyMs(latency)
                .provider(getName())
                .model("anthropic.claude-v3")
                .build();

        return AiResponse.builder()
                .generatedText(generated)
                .finishReason("stop")
                .usageStatistics(usage)
                .safetyMetadata(Map.of("guardrails_passed", true))
                .latencyMs(latency)
                .providerMetadata(Map.of("aws_request_id", "aws-req-123"))
                .build();
    }

    @Override
    public void executeStream(AiRequest request, AiStreamingHandler handler) {
        AiResponse response = execute(request);
        handler.onChunk("[Bedrock Stream Chunk] ");
        handler.onComplete(response);
    }

    @Override
    public boolean supportsCapability(AiModelCapability capability) {
        return capability == AiModelCapability.CHAT || capability == AiModelCapability.TOOL_CALLING;
    }

    @Override
    public List<String> getDiscoverableModels() {
        return List.of("anthropic.claude-v3", "meta.llama3");
    }

    @Override
    public int getDefaultPriority() {
        return 75;
    }

    @Override
    public boolean isHealthy() {
        return true;
    }
}
