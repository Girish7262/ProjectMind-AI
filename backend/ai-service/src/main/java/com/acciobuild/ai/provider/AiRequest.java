package com.acciobuild.ai.provider;

import com.acciobuild.ai.dto.ToolDefinitionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates full configuration, input prompts, system contexts, safety parameters,
 * and tools registered for execution on LLM providers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String prompt;
    private String context;
    private String memory;
    private List<ToolDefinitionDto> tools;
    private Map<String, Object> metadata;
    private Integer tokenBudget;
    private Double temperature;
    private Double topP;
    private Integer maxTokens;
    private List<String> stopSequences;
    private String responseFormat;
}
