package com.acciobuild.knowledge.enums;

/**
 * Lifecycle states of an offline or asynchronous embedding generation job.
 */
public enum EmbeddingJobStatus {
    PENDING,
    QUEUED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
