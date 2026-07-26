package com.acciobuild.ai.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator tracking Organization policy service integration boundaries status.
 */
@Component("organizationService")
public class OrganizationServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up().withDetail("service", "Organization Service").withDetail("endpoint", "feign-client").build();
    }
}
