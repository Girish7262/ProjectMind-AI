package com.acciobuild.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

/**
 * ConfigurationProperties class mapping configuration profiles for multiple embedding providers.
 */
@Configuration
@ConfigurationProperties(prefix = "acciobuild.ai.embedding")
@Data
public class EmbeddingProviderConfigProperties {

    private String activeProvider = "openai";
    private Map<String, ProviderSettings> providers;

    /**
     * Settings profile mapping properties for a single provider.
     */
    @Data
    public static class ProviderSettings {
        private String apiKey;
        private String endpointUrl;
        private String defaultModel;
        private int defaultDimension = 1536;
        private Map<String, String> additionalProperties;
    }
}
