package com.acciobuild.knowledge.service;

import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import com.acciobuild.knowledge.enums.EmbeddingJobStatus;
import java.util.List;
import java.util.UUID;

/**
 * Business service interface managing the persistence and transitions of embedding generation jobs.
 */
public interface EmbeddingJobService {

    /**
     * Initialize and persist a new embedding job for a document.
     */
    EmbeddingJob createJob(UUID documentId);

    /**
     * Transition the status of an existing embedding job.
     */
    void updateJobStatus(UUID jobId, EmbeddingJobStatus status);

    /**
     * Record a failed processing attempt and increment the job retry count.
     */
    void incrementRetryCount(UUID jobId, String errorMessage);

    /**
     * Append a detailed step execution record to the processing logs.
     */
    void logProcessingStep(UUID jobId, String stepName, long durationMs, String status, String message);

    /**
     * Mark an embedding job as completed, updating metadata statistics.
     */
    void completeJob(UUID jobId, double cost);

    /**
     * Mark an embedding job as permanently failed.
     */
    void failJob(UUID jobId, String errorMessage);

    /**
     * Retrieve all jobs matching specified status constraints.
     */
    List<EmbeddingJob> getJobsByStatus(EmbeddingJobStatus status);
}
