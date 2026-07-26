package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for EmbeddingJob.
 */
@Repository
public interface EmbeddingJobRepository extends JpaRepository<EmbeddingJob, UUID> {

    /**
     * Finds embedding jobs by document ID.
     */
    List<EmbeddingJob> findByDocumentId(UUID documentId);

    /**
     * Finds embedding jobs by status.
     */
    List<EmbeddingJob> findByStatus(com.acciobuild.knowledge.enums.EmbeddingJobStatus status);
}
