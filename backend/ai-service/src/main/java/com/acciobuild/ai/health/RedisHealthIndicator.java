package com.acciobuild.ai.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Health indicator tracking Redis ping response connectivity.
 */
@Component("redisCustom")
public class RedisHealthIndicator implements HealthIndicator {
    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String ping = conn.ping();
            if ("PONG".equals(ping)) {
                return Health.up().withDetail("ping", "PONG").build();
            }
            return Health.down().withDetail("ping", ping).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
