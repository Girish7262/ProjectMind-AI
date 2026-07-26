package com.acciobuild.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

/**
 * Tracing configuration mapping OpenTelemetry reverse proxy options.
 */
@Configuration
public class TracingConfiguration {

    @Bean
    public Map<String, Object> openTelemetryTraceSettings() {
        return Map.of("telemetry", "active", "exporter", "otlp");
    }
}
