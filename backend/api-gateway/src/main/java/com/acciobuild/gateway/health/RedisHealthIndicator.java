package com.acciobuild.gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Non-blocking reactive health indicator checking Redis cluster connection.
 */
@Component("redisCustom")
public class RedisHealthIndicator implements ReactiveHealthIndicator {
    private final ReactiveRedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(ReactiveRedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<Health> health() {
        return connectionFactory.getReactiveConnection().ping()
                .map(ping -> "PONG".equals(ping)
                        ? Health.up().withDetail("ping", "PONG").build()
                        : Health.down().withDetail("ping", ping).build())
                .onErrorResume(ex -> Mono.just(Health.down(ex).build()));
    }
}
