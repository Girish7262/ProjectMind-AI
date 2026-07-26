package com.acciobuild.knowledge.health;

import com.acciobuild.knowledge.enums.EmbeddingJobStatus;
import com.acciobuild.knowledge.service.EmbeddingJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator monitoring size bounds of the active asynchronous embedding jobs queue.
 */
@Component
@RequiredArgsConstructor
public class EmbeddingQueueHealthIndicator implements HealthIndicator {

    private final EmbeddingJobService jobService;

    @Override
    public Health health() {
        try {
            long queuedCount = jobService.getJobsByStatus(EmbeddingJobStatus.QUEUED).size();
            long pendingCount = jobService.getJobsByStatus(EmbeddingJobStatus.PENDING).size();
            long totalQueueSize = queuedCount + pendingCount;

            Health.Builder builder = totalQueueSize > 500 ? Health.status("WARN") : Health.up();
            return builder
                    .withDetail("queuedSize", queuedCount)
                    .withDetail("pendingSize", pendingCount)
                    .withDetail("totalQueueSize", totalQueueSize)
                    .withDetail("warnThreshold", 500)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
