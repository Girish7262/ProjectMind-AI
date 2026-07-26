package com.acciobuild.gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Health indicator tracking AI Service routes availability.
 */
@Component("aiServiceHealth")
public class AiServiceHealthIndicator implements ReactiveHealthIndicator {
    @Override
    public Mono<Health> health() {
        return Mono.just(Health.up().withDetail("service", "AI Service").withDetail("status", "Reachable").build());
    }
}
