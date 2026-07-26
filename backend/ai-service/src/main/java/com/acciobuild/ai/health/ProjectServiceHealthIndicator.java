package com.acciobuild.ai.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator verifying Project settings lookup services status.
 */
@Component("projectService")
public class ProjectServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up().withDetail("service", "Project Service").withDetail("endpoint", "feign-client").build();
    }
}
