package com.acciobuild.knowledge.health;

import com.acciobuild.knowledge.domain.repository.KnowledgeSearchIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator tracking count status of searchable metadata indices.
 */
@Component
@RequiredArgsConstructor
public class SearchIndexHealthIndicator implements HealthIndicator {

    private final KnowledgeSearchIndexRepository indexRepository;

    @Override
    public Health health() {
        try {
            long count = indexRepository.count();
            return Health.up()
                    .withDetail("searchIndexRecordCount", count)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
