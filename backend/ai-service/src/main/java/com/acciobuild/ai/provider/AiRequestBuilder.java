package com.acciobuild.ai.provider;

import com.acciobuild.ai.dto.ToolDefinitionDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for assembling and validating {@link AiRequest} payloads.
 */
public class AiRequestBuilder {
    private String prompt;
    private String context;
    private String memory;
    private List<ToolDefinitionDto> tools = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();
    private Integer tokenBudget;
    private Double temperature = 0.7;
    private Double topP = 1.0;
    private Integer maxTokens = 1000;
    private List<String> stopSequences = new ArrayList<>();
    private String responseFormat = "text";

    public AiRequestBuilder prompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public AiRequestBuilder context(String context) {
        this.context = context;
        return this;
    }

    public AiRequestBuilder memory(String memory) {
        this.memory = memory;
        return this;
    }

    public AiRequestBuilder tools(List<ToolDefinitionDto> tools) {
        if (tools != null) {
            this.tools = new ArrayList<>(tools);
        }
        return this;
    }

    public AiRequestBuilder addTool(ToolDefinitionDto tool) {
        if (tool != null) {
            this.tools.add(tool);
        }
        return this;
    }

    public AiRequestBuilder metadata(Map<String, Object> metadata) {
        if (metadata != null) {
            this.metadata = new HashMap<>(metadata);
        }
        return this;
    }

    public AiRequestBuilder addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public AiRequestBuilder tokenBudget(Integer tokenBudget) {
        this.tokenBudget = tokenBudget;
        return this;
    }

    public AiRequestBuilder temperature(Double temperature) {
        if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
            throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
        }
        this.temperature = temperature;
        return this;
    }

    public AiRequestBuilder topP(Double topP) {
        if (topP != null && (topP < 0.0 || topP > 1.0)) {
            throw new IllegalArgumentException("TopP must be between 0.0 and 1.0");
        }
        this.topP = topP;
        return this;
    }

    public AiRequestBuilder maxTokens(Integer maxTokens) {
        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("MaxTokens must be greater than 0");
        }
        this.maxTokens = maxTokens;
        return this;
    }

    public AiRequestBuilder stopSequences(List<String> stopSequences) {
        if (stopSequences != null) {
            this.stopSequences = new ArrayList<>(stopSequences);
        }
        return this;
    }

    public AiRequestBuilder responseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    public AiRequest build() {
        if (prompt == null || prompt.strip().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty.");
        }
        return new AiRequest(prompt, context, memory, tools, metadata, tokenBudget, temperature, topP, maxTokens, stopSequences, responseFormat);
    }
}
