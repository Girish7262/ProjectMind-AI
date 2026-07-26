package com.acciobuild.knowledge.observability;

import com.acciobuild.knowledge.health.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test validating the initialization, status values, and parameters of custom health check indicators.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ResilienceAndHealthTest {

    @MockitoBean
    private com.acciobuild.common.security.JwtUtils jwtUtils;

    @Autowired
    private KnowledgeDatabaseHealthIndicator databaseHealthIndicator;

    @Autowired
    private OutboxHealthIndicator outboxHealthIndicator;

    @Autowired
    private SearchIndexHealthIndicator searchIndexHealthIndicator;

    @Autowired
    private ChunkPreparationHealthIndicator chunkPreparationHealthIndicator;

    @Autowired
    private EmbeddingQueueHealthIndicator embeddingQueueHealthIndicator;

    @Test
    void testHealthIndicators() {
        assertNotNull(databaseHealthIndicator);
        assertNotNull(outboxHealthIndicator);
        assertNotNull(searchIndexHealthIndicator);
        assertNotNull(chunkPreparationHealthIndicator);
        assertNotNull(embeddingQueueHealthIndicator);

        Health dbHealth = databaseHealthIndicator.health();
        assertEquals(Status.UP, dbHealth.getStatus());

        Health outboxHealth = outboxHealthIndicator.health();
        assertNotNull(outboxHealth.getStatus());

        Health indexHealth = searchIndexHealthIndicator.health();
        assertEquals(Status.UP, indexHealth.getStatus());

        Health chunkHealth = chunkPreparationHealthIndicator.health();
        assertEquals(Status.UP, chunkHealth.getStatus());

        Health queueHealth = embeddingQueueHealthIndicator.health();
        assertEquals(Status.UP, queueHealth.getStatus());
    }
}
