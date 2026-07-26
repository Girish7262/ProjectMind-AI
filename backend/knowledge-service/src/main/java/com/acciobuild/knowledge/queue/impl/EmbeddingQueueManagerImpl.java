package com.acciobuild.knowledge.queue.impl;

import com.acciobuild.knowledge.domain.event.EmbeddingQueuedEvent;
import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.enums.EmbeddingJobStatus;
import com.acciobuild.knowledge.queue.EmbeddingQueueManager;
import com.acciobuild.knowledge.service.EmbeddingJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enterprise implementation of the EmbeddingQueueManager.
 * Manages prioritization, retries, and dead-letter routing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingQueueManagerImpl implements EmbeddingQueueManager {

    private final EmbeddingJobService jobService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository chunkRepository;

    private static final int MAX_RETRIES = 3;

    @Override
    public void enqueue(EmbeddingJob job) {
        log.info("Queue Manager: Enqueueing job ID: {} for document ID: {}", job.getId(), job.getDocument().getId());
        jobService.updateJobStatus(job.getId(), EmbeddingJobStatus.QUEUED);

        int maxPriority = 0;
        List<KnowledgeDocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(job.getDocument().getId());
        if (chunks != null && !chunks.isEmpty()) {
            maxPriority = chunks.stream()
                    .mapToInt(KnowledgeDocumentChunk::getPriority)
                    .max()
                    .orElse(0);
        }

        // Publish queued event
        eventPublisher.publishEvent(new EmbeddingQueuedEvent(
                job.getDocument().getOrganizationId(),
                job.getDocument().getId(),
                job.getId(),
                maxPriority,
                UUID.randomUUID().toString()
        ));
    }

    @Override
    public EmbeddingJob poll() {
        List<EmbeddingJob> queuedJobs = jobService.getJobsByStatus(EmbeddingJobStatus.QUEUED);
        if (queuedJobs.isEmpty()) {
            return null;
        }

        // Prioritize:
        // 1. Highest max chunk priority (descending order)
        // 2. Earliest creation time (ascending order)
        EmbeddingJob highestPriorityJob = queuedJobs.stream()
                .min(Comparator.comparingInt(this::getMaxChunkPriority).reversed()
                        .thenComparing(EmbeddingJob::getCreatedAt))
                .orElse(null);

        if (highestPriorityJob != null) {
            log.info("Queue Manager: Polled job ID: {} (priority={})", highestPriorityJob.getId(), getMaxChunkPriority(highestPriorityJob));
            jobService.updateJobStatus(highestPriorityJob.getId(), EmbeddingJobStatus.IN_PROGRESS);
        }

        return highestPriorityJob;
    }

    @Override
    public void handleFailure(UUID jobId, String errorMessage) {
        List<EmbeddingJob> queuedAndInProgress = jobService.getJobsByStatus(EmbeddingJobStatus.IN_PROGRESS);
        EmbeddingJob job = queuedAndInProgress.stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst()
                .orElseGet(() -> {
                    // Fallback to find by ID if status was changed
                    List<EmbeddingJob> allQueued = jobService.getJobsByStatus(EmbeddingJobStatus.QUEUED);
                    return allQueued.stream()
                            .filter(j -> j.getId().equals(jobId))
                            .findFirst()
                            .orElse(null);
                });

        if (job == null) {
            log.error("Queue Manager: Could not find active job ID: {}", jobId);
            return;
        }

        if (job.getRetryCount() < MAX_RETRIES) {
            jobService.incrementRetryCount(job.getId(), errorMessage);
            jobService.logProcessingStep(job.getId(), "RETRY_QUEUED", 0, "QUEUED", "Retrying job: " + errorMessage);
            enqueue(job);
        } else {
            log.error("Queue Manager: Job ID: {} exceeded maximum retries. Routing to Dead Letter Queue.", jobId);
            jobService.failJob(job.getId(), "Max retries exceeded. Last error: " + errorMessage);
            jobService.logProcessingStep(job.getId(), "DLQ_ROUTING", 0, "FAILED", "Max retries exceeded: " + errorMessage);
        }
    }

    @Override
    public List<EmbeddingJob> getDeadLetterJobs() {
        return jobService.getJobsByStatus(EmbeddingJobStatus.FAILED);
    }

    private int getMaxChunkPriority(EmbeddingJob job) {
        if (job.getDocument() == null) {
            return 0;
        }
        List<KnowledgeDocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(job.getDocument().getId());
        if (chunks == null || chunks.isEmpty()) {
            return 0;
        }
        return chunks.stream()
                .mapToInt(KnowledgeDocumentChunk::getPriority)
                .max()
                .orElse(0);
    }
}
