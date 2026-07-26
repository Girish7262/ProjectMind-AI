package com.acciobuild.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracing configuration mapping distributed trace mock settings.
 */
@Configuration
public class TracingConfiguration {

    @Bean
    public Map<String, Object> openTelemetryTracer() {
        Map<String, Object> tracingMock = new HashMap<>();
        tracingMock.put("provider", "OpenTelemetryMock");
        tracingMock.put("active", true);
        return tracingMock;
    }
}
