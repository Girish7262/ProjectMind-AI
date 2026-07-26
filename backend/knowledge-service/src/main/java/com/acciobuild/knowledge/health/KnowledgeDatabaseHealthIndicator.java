package com.acciobuild.knowledge.health;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator verifying active database connections and basic query processing.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeDatabaseHealthIndicator implements HealthIndicator {

    private final EntityManager entityManager;

    @Override
    public Health health() {
        try {
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();
            if (result != null && result.toString().equals("1")) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("connectivity", "OK")
                        .build();
            }
            return Health.down().withDetail("error", "Unexpected validation query result").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
