package com.acciobuild.project.health;

import com.acciobuild.project.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Actuator Health Indicator tracking transactional Outbox failed backlogs.
 */
@Component
@RequiredArgsConstructor
public class OutboxHealthIndicator implements HealthIndicator {

    private final OutboxEventRepository outboxRepository;

    @Override
    public Health health() {
        try {
            long failedCount = outboxRepository.findByStatusOrderByCreatedAtAsc("FAILED").size();
            
            if (failedCount > 10) {
                return Health.down()
                        .withDetail("failed_outbox_events", failedCount)
                        .withDetail("message", "Outbox failure backlog exceeds critical threshold (10).")
                        .build();
            }
            
            return Health.up()
                    .withDetail("failed_outbox_events", failedCount)
                    .withDetail("status", "Outbox processing healthy.")
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
