package com.acciobuild.gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator verifying JVM memory threshold bounds.
 */
@Component("gatewayHealth")
public class GatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        long freeMem = Runtime.getRuntime().freeMemory();
        if (freeMem > 1024 * 1024) {
            return Health.up().withDetail("freeMemory", freeMem).build();
        }
        return Health.down().withDetail("freeMemory", freeMem).withDetail("error", "OutOfMemory safety threshold").build();
    }
}
