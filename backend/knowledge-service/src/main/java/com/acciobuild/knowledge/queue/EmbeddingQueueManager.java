package com.acciobuild.knowledge.queue;

import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import java.util.List;
import java.util.UUID;

/**
 * Queue manager interface handling job priority, retry counts, routing to dead-letter, and tenant isolation.
 */
public interface EmbeddingQueueManager {

    /**
     * Submit an embedding job to the processing queue.
     */
    void enqueue(EmbeddingJob job);

    /**
     * Retrieve and remove the next highest-priority job from the queue.
     */
    EmbeddingJob poll();

    /**
     * Re-schedule a failed job for execution if retries are not exhausted, or route to DLQ.
     */
    void handleFailure(UUID jobId, String errorMessage);

    /**
     * Retrieve all permanently failed/dead-lettered embedding jobs.
     */
    List<EmbeddingJob> getDeadLetterJobs();
}
