package com.acciobuild.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Output abstraction containing generated texts, safety flags, and usage statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String generatedText;
    private String finishReason;
    private AiTokenUsage usageStatistics;
    private Map<String, Object> safetyMetadata;
    private long latencyMs;
    private Map<String, Object> providerMetadata;
}
