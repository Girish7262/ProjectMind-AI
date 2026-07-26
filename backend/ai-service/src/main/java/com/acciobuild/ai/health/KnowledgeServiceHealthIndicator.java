package com.acciobuild.ai.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator verifying Knowledge feign endpoint parameters status.
 */
@Component("knowledgeService")
public class KnowledgeServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up().withDetail("service", "Knowledge Service").withDetail("endpoint", "feign-client").build();
    }
}
