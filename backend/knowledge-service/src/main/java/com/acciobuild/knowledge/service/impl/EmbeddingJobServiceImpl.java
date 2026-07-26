package com.acciobuild.knowledge.service.impl;

import com.acciobuild.knowledge.ai.TokenEstimator;
import com.acciobuild.knowledge.domain.event.EmbeddingCompletedEvent;
import com.acciobuild.knowledge.domain.event.EmbeddingFailedEvent;
import com.acciobuild.knowledge.domain.event.EmbeddingRequestedEvent;
import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import com.acciobuild.knowledge.domain.model.EmbeddingProcessingLog;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.domain.repository.EmbeddingJobRepository;
import com.acciobuild.knowledge.domain.repository.EmbeddingProcessingLogRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.enums.EmbeddingJobStatus;
import com.acciobuild.knowledge.service.EmbeddingJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of EmbeddingJobService managing state transitions and publishing outbox events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingJobServiceImpl implements EmbeddingJobService {

    private final EmbeddingJobRepository jobRepository;
    private final EmbeddingProcessingLogRepository logRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository chunkRepository;
    private final TokenEstimator tokenEstimator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EmbeddingJob createJob(UUID documentId) {
        log.info("AI Platform: Initializing embedding generation job for document ID: {}", documentId);
        KnowledgeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        int totalTokens = 0;
        double totalCost = 0.0;

        List<KnowledgeDocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
        if (chunks != null) {
            for (KnowledgeDocumentChunk chunk : chunks) {
                int tokens = tokenEstimator.estimateTokens(chunk.getContent());
                totalTokens += tokens;
                totalCost += tokenEstimator.calculateCost(chunk.getContent(), "text-embedding-3-small");
            }
        }

        EmbeddingJob job = new EmbeddingJob();
        job.setId(UUID.randomUUID());
        job.setDocument(doc);
        job.setProvider("OPENAI");
        job.setModel("text-embedding-3-small");
        job.setStatus(EmbeddingJobStatus.PENDING);
        job.setRetryCount(0);
        job.setEstimatedTokens(totalTokens);
        job.setEstimatedCost(totalCost);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        EmbeddingJob savedJob = jobRepository.save(job);

        // Publish event (transactional outbox interceptable)
        eventPublisher.publishEvent(new EmbeddingRequestedEvent(
                doc.getOrganizationId(),
                doc.getId(),
                savedJob.getId(),
                UUID.randomUUID().toString()
        ));

        return savedJob;
    }

    @Override
    @Transactional
    public void updateJobStatus(UUID jobId, EmbeddingJobStatus status) {
        log.trace("AI Platform: Transitioning job ID: {} to status: {}", jobId, status);
        EmbeddingJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public void incrementRetryCount(UUID jobId, String errorMessage) {
        log.warn("AI Platform: Incrementing retry counter for job ID: {} due to error: {}", jobId, errorMessage);
        EmbeddingJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(errorMessage);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public void logProcessingStep(UUID jobId, String stepName, long durationMs, String status, String message) {
        log.trace("AI Platform Log: Step '{}' on job ID: {} completed with status: {}", stepName, jobId, status);
        EmbeddingProcessingLog processingLog = new EmbeddingProcessingLog();
        processingLog.setId(UUID.randomUUID());
        processingLog.setJob(jobRepository.getReferenceById(jobId));
        processingLog.setStepName(stepName);
        processingLog.setDurationMs(durationMs);
        processingLog.setStatus(status);
        processingLog.setMessage(message);
        processingLog.setCreatedAt(LocalDateTime.now());
        logRepository.save(processingLog);
    }

    @Override
    @Transactional
    public void completeJob(UUID jobId, double cost) {
        log.info("AI Platform: Embedding job completed successfully for job ID: {}", jobId);
        EmbeddingJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        job.setStatus(EmbeddingJobStatus.COMPLETED);
        job.setEstimatedCost(cost);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        List<KnowledgeDocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(job.getDocument().getId());
        eventPublisher.publishEvent(new EmbeddingCompletedEvent(
                job.getDocument().getOrganizationId(),
                job.getDocument().getId(),
                job.getId(),
                chunks != null ? chunks.size() : 0,
                cost,
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional
    public void failJob(UUID jobId, String errorMessage) {
        log.error("AI Platform: Permanent failure recorded for job ID: {}. Error: {}", jobId, errorMessage);
        EmbeddingJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        job.setStatus(EmbeddingJobStatus.FAILED);
        job.setErrorMessage(errorMessage);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        eventPublisher.publishEvent(new EmbeddingFailedEvent(
                job.getDocument().getOrganizationId(),
                job.getDocument().getId(),
                job.getId(),
                errorMessage,
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmbeddingJob> getJobsByStatus(EmbeddingJobStatus status) {
        return jobRepository.findByStatus(status);
    }
}
