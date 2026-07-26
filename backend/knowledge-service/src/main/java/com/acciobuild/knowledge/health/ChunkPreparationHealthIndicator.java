package com.acciobuild.knowledge.health;

import com.acciobuild.knowledge.service.ChunkPreparationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator verifying existence and injection of ChunkPreparationService.
 */
@Component
public class ChunkPreparationHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private ChunkPreparationService chunkPreparationService;

    @Override
    public Health health() {
        if (chunkPreparationService != null) {
            return Health.up().withDetail("service", "Active").build();
        }
        return Health.down().withDetail("error", "Chunk preparation service bean not resolved").build();
    }
}
