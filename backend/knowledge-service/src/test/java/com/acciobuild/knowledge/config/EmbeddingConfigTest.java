package com.acciobuild.knowledge.config;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test validating configuration property profiles and bindings.
 */
public class EmbeddingConfigTest {

    @Test
    void testConfigBinding() {
        EmbeddingProviderConfigProperties config = new EmbeddingProviderConfigProperties();
        config.setActiveProvider("openai");

        EmbeddingProviderConfigProperties.ProviderSettings settings = new EmbeddingProviderConfigProperties.ProviderSettings();
        settings.setApiKey("vault:test-key");
        settings.setEndpointUrl("http://localhost:8080/v1");
        settings.setDefaultModel("text-embedding-3-small");
        settings.setDefaultDimension(1536);

        Map<String, EmbeddingProviderConfigProperties.ProviderSettings> providers = new HashMap<>();
        providers.put("openai", settings);
        config.setProviders(providers);

        assertEquals("openai", config.getActiveProvider());
        assertNotNull(config.getProviders());
        assertEquals("vault:test-key", config.getProviders().get("openai").getApiKey());
        assertEquals("http://localhost:8080/v1", config.getProviders().get("openai").getEndpointUrl());
        assertEquals("text-embedding-3-small", config.getProviders().get("openai").getDefaultModel());
        assertEquals(1536, config.getProviders().get("openai").getDefaultDimension());
    }
}
