package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.EmbeddingProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for EmbeddingProcessingLog.
 */
@Repository
public interface EmbeddingProcessingLogRepository extends JpaRepository<EmbeddingProcessingLog, UUID> {

    /**
     * Finds processing logs by job ID.
     */
    List<EmbeddingProcessingLog> findByJobIdOrderByCreatedAtAsc(UUID jobId);
}
