package com.acciobuild.knowledge.scheduler;

import com.acciobuild.knowledge.ai.EmbeddingClient;
import com.acciobuild.knowledge.ai.VectorStoreAdapter;
import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.multitenancy.TenantContext;
import com.acciobuild.knowledge.queue.EmbeddingQueueManager;
import com.acciobuild.knowledge.service.EmbeddingJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Background scheduler processing queued embedding generation jobs.
 * Enforces tenant separation during asynchronous execution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingJobScheduler {

    private final EmbeddingQueueManager queueManager;
    private final EmbeddingJobService jobService;
    private final EmbeddingClient embeddingClient;
    private final VectorStoreAdapter vectorStoreAdapter;
    private final com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository chunkRepository;

    /**
     * Periodically check for queued embedding requests.
     */
    @Scheduled(fixedDelayString = "${acciobuild.ai.embedding.scheduler-delay-ms:5000}")
    public void processNextJob() {
        EmbeddingJob job = queueManager.poll();
        if (job == null) {
            return;
        }

        UUID orgId = job.getDocument().getOrganizationId();
        TenantContext.setCurrentTenant(orgId);
        long startTime = System.currentTimeMillis();

        try {
            log.info("Job Scheduler: Processing job ID: {} for tenant Org: {}", job.getId(), orgId);
            jobService.logProcessingStep(job.getId(), "STARTED", 0, "IN_PROGRESS", "Starting job processing on background thread.");

            List<KnowledgeDocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(job.getDocument().getId());
            if (chunks == null || chunks.isEmpty()) {
                throw new IllegalArgumentException("No text chunks found for embedding generation.");
            }

            List<String> contents = chunks.stream()
                    .map(KnowledgeDocumentChunk::getContent)
                    .collect(Collectors.toList());

            List<UUID> chunkIds = chunks.stream()
                    .map(KnowledgeDocumentChunk::getId)
                    .collect(Collectors.toList());

            // 1. Generate embeddings using stub client
            long embedStart = System.currentTimeMillis();
            List<List<Double>> vectors = embeddingClient.embedBatch(contents);
            long embedDuration = System.currentTimeMillis() - embedStart;
            jobService.logProcessingStep(job.getId(), "EMBEDDING_GENERATION", embedDuration, "SUCCESS", "Generated " + vectors.size() + " vectors.");

            // 2. Persist vector models
            long dbStart = System.currentTimeMillis();
            vectorStoreAdapter.saveEmbeddings(job.getDocument().getId(), chunkIds, vectors);
            long dbDuration = System.currentTimeMillis() - dbStart;
            jobService.logProcessingStep(job.getId(), "VECTOR_STORAGE", dbDuration, "SUCCESS", "Saved vectors to store.");

            // 3. Mark successful completion
            long totalDuration = System.currentTimeMillis() - startTime;
            jobService.logProcessingStep(job.getId(), "JOB_COMPLETED", totalDuration, "SUCCESS", "Embedding workflow completed successfully.");
            jobService.completeJob(job.getId(), job.getEstimatedCost());

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            log.error("Job Scheduler: Failed processing job ID: {} due to error: {}", job.getId(), e.getMessage());
            jobService.logProcessingStep(job.getId(), "JOB_FAILED", totalDuration, "FAILED", e.getMessage());
            queueManager.handleFailure(job.getId(), e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}
