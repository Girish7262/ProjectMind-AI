package com.acciobuild.knowledge.health;

import com.acciobuild.knowledge.domain.repository.KnowledgeOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator tracking queue size of outbox publishing log entries.
 */
@Component
@RequiredArgsConstructor
public class OutboxHealthIndicator implements HealthIndicator {

    private final KnowledgeOutboxRepository outboxRepository;

    @Override
    public Health health() {
        try {
            long pendingCount = outboxRepository.countByStatus("PENDING");
            Health.Builder builder = pendingCount > 100 ? Health.status("WARN") : Health.up();
            return builder
                    .withDetail("pendingEvents", pendingCount)
                    .withDetail("maxThreshold", 100)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
