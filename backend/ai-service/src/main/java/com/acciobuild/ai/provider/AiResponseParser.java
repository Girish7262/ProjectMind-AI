package com.acciobuild.ai.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for translating raw provider payloads and nested JSON elements into structured responses.
 */
@Component
@Slf4j
public class AiResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public AiResponse parseRawResponse(Object rawPayload, String provider, String model, long latencyMs) {
        log.info("Parsing raw response for provider: {}, model: {}", provider, model);
        if (rawPayload == null) {
            return AiResponse.builder()
                    .generatedText("")
                    .finishReason("error")
                    .latencyMs(latencyMs)
                    .build();
        }

        try {
            Map<String, Object> map;
            if (rawPayload instanceof Map) {
                map = (Map<String, Object>) rawPayload;
            } else if (rawPayload instanceof String str) {
                map = objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {});
            } else {
                return buildFallbackResponse(rawPayload.toString(), provider, model, latencyMs);
            }

            String text = getNestedString(map, "text", "generatedText", "choices[0].message.content", "content");
            String finishReason = getNestedString(map, "finishReason", "choices[0].finish_reason", "finish_reason");
            if (finishReason == null || finishReason.strip().isEmpty()) {
                finishReason = "stop";
            }

            int promptTokens = getNestedInteger(map, "usage.prompt_tokens", "input_tokens", "promptTokens");
            int completionTokens = getNestedInteger(map, "usage.completion_tokens", "output_tokens", "completionTokens");
            double cost = getNestedDouble(map, "usage.cost", "estimatedCost", "cost");

            AiTokenUsage usage = AiTokenUsage.builder()
                    .inputTokens(promptTokens > 0 ? promptTokens : 12)
                    .outputTokens(completionTokens > 0 ? completionTokens : 24)
                    .estimatedCost(cost > 0.0 ? cost : (promptTokens + completionTokens) * 0.00002)
                    .latencyMs(latencyMs)
                    .provider(provider)
                    .model(model)
                    .build();

            Map<String, Object> safety = (Map<String, Object>) map.getOrDefault("safetyMetadata", new HashMap<>());
            Map<String, Object> providerMeta = (Map<String, Object>) map.getOrDefault("providerMetadata", map);

            return AiResponse.builder()
                    .generatedText(text != null ? text : rawPayload.toString())
                    .finishReason(finishReason)
                    .usageStatistics(usage)
                    .safetyMetadata(safety)
                    .latencyMs(latencyMs)
                    .providerMetadata(providerMeta)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse raw response. Falling back to text wrap.", e);
            return buildFallbackResponse(rawPayload.toString(), provider, model, latencyMs);
        }
    }

    private AiResponse buildFallbackResponse(String text, String provider, String model, long latencyMs) {
        AiTokenUsage usage = AiTokenUsage.builder()
                .inputTokens(15)
                .outputTokens(35)
                .estimatedCost(50 * 0.00002)
                .latencyMs(latencyMs)
                .provider(provider)
                .model(model)
                .build();

        return AiResponse.builder()
                .generatedText(text)
                .finishReason("stop")
                .usageStatistics(usage)
                .latencyMs(latencyMs)
                .safetyMetadata(new HashMap<>())
                .providerMetadata(new HashMap<>())
                .build();
    }

    private String getNestedString(Map<String, Object> map, String... paths) {
        for (String path : paths) {
            Object val = getNestedValue(map, path);
            if (val != null) {
                return val.toString();
            }
        }
        return null;
    }

    private int getNestedInteger(Map<String, Object> map, String... paths) {
        for (String path : paths) {
            Object val = getNestedValue(map, path);
            if (val instanceof Number num) {
                return num.intValue();
            }
        }
        return 0;
    }

    private double getNestedDouble(Map<String, Object> map, String... paths) {
        for (String path : paths) {
            Object val = getNestedValue(map, path);
            if (val instanceof Number num) {
                return num.doubleValue();
            }
        }
        return 0.0;
    }

    private Object getNestedValue(Map<String, Object> map, String path) {
        if (!path.contains(".")) {
            return map.get(path);
        }
        String[] keys = path.split("\\.");
        Object current = map;
        for (String key : keys) {
            if (current instanceof Map<?, ?> m) {
                current = m.get(key);
            } else {
                return null;
            }
        }
        return current;
    }
}
