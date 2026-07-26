package com.acciobuild.ai.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Health indicator tracking active PostgreSQL connection state.
 */
@Component("postgresql")
public class PostgresHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    public PostgresHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return Health.up().withDetail("database", "PostgreSQL").withDetail("status", "Active connection").build();
            }
            return Health.down().withDetail("error", "Invalid connection check").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
