package com.acciobuild.ai.health;

import com.acciobuild.ai.tool.ToolRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator tracking discovered RAG tools status in ToolRegistry.
 */
@Component("toolRegistryCustom")
public class ToolRegistryHealthIndicator implements HealthIndicator {
    private final ToolRegistry registry;

    public ToolRegistryHealthIndicator(ToolRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Health health() {
        int toolsCount = registry.getAllTools().size();
        if (toolsCount > 0) {
            return Health.up().withDetail("discoveredTools", toolsCount).build();
        }
        return Health.down().withDetail("error", "No RAG tools discovered in context").build();
    }
}
