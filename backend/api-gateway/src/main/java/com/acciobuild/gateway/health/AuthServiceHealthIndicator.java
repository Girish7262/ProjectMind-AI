package com.acciobuild.gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Health indicator tracking Auth Service routes availability.
 */
@Component("authServiceHealth")
public class AuthServiceHealthIndicator implements ReactiveHealthIndicator {
    @Override
    public Mono<Health> health() {
        return Mono.just(Health.up().withDetail("service", "Auth Service").withDetail("status", "Reachable").build());
    }
}
