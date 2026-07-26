package com.acciobuild.knowledge.queue;

import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.enums.EmbeddingJobStatus;
import com.acciobuild.knowledge.queue.impl.EmbeddingQueueManagerImpl;
import com.acciobuild.knowledge.service.EmbeddingJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test validating queue prioritization, retries, DLQ routing, and event triggers.
 */
@ExtendWith(MockitoExtension.class)
public class EmbeddingQueueTest {

    @Mock
    private EmbeddingJobService jobService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository chunkRepository;

    @InjectMocks
    private EmbeddingQueueManagerImpl queueManager;

    private EmbeddingJob job1;
    private EmbeddingJob job2;
    private List<KnowledgeDocumentChunk> chunks1;
    private List<KnowledgeDocumentChunk> chunks2;

    @BeforeEach
    void setUp() {
        KnowledgeDocument doc1 = new KnowledgeDocument();
        doc1.setId(UUID.randomUUID());
        doc1.setOrganizationId(UUID.randomUUID());
        KnowledgeDocumentChunk chunk1 = new KnowledgeDocumentChunk();
        chunk1.setPriority(1);
        chunk1.setDocument(doc1);
        chunks1 = Collections.singletonList(chunk1);

        job1 = new EmbeddingJob();
        job1.setId(UUID.randomUUID());
        job1.setDocument(doc1);
        job1.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        job1.setStatus(EmbeddingJobStatus.PENDING);

        KnowledgeDocument doc2 = new KnowledgeDocument();
        doc2.setId(UUID.randomUUID());
        doc2.setOrganizationId(UUID.randomUUID());
        KnowledgeDocumentChunk chunk2 = new KnowledgeDocumentChunk();
        chunk2.setPriority(10); // Higher chunk priority
        chunk2.setDocument(doc2);
        chunks2 = Collections.singletonList(chunk2);

        job2 = new EmbeddingJob();
        job2.setId(UUID.randomUUID());
        job2.setDocument(doc2);
        job2.setCreatedAt(LocalDateTime.now());
        job2.setStatus(EmbeddingJobStatus.PENDING);
    }

    @Test
    void testEnqueue_Success() {
        when(chunkRepository.findByDocumentIdOrderByChunkIndexAsc(any())).thenReturn(chunks1);
        queueManager.enqueue(job1);

        verify(jobService).updateJobStatus(job1.getId(), EmbeddingJobStatus.QUEUED);
        verify(eventPublisher).publishEvent(any(com.acciobuild.knowledge.domain.event.EmbeddingQueuedEvent.class));
    }

    @Test
    void testPoll_Prioritization() {
        job1.setStatus(EmbeddingJobStatus.QUEUED);
        job2.setStatus(EmbeddingJobStatus.QUEUED);

        when(jobService.getJobsByStatus(EmbeddingJobStatus.QUEUED))
                .thenReturn(Arrays.asList(job1, job2));
        when(chunkRepository.findByDocumentIdOrderByChunkIndexAsc(job1.getDocument().getId()))
                .thenReturn(chunks1);
        when(chunkRepository.findByDocumentIdOrderByChunkIndexAsc(job2.getDocument().getId()))
                .thenReturn(chunks2);

        EmbeddingJob polled = queueManager.poll();

        assertNotNull(polled);
        assertEquals(job2.getId(), polled.getId()); // Should select highest priority chunk job
    }

    @Test
    void testHandleFailure_RetrySupport() {
        job1.setStatus(EmbeddingJobStatus.IN_PROGRESS);
        job1.setRetryCount(0);

        when(jobService.getJobsByStatus(EmbeddingJobStatus.IN_PROGRESS))
                .thenReturn(Collections.singletonList(job1));
        when(chunkRepository.findByDocumentIdOrderByChunkIndexAsc(any())).thenReturn(chunks1);

        queueManager.handleFailure(job1.getId(), "Network error");

        verify(jobService).incrementRetryCount(job1.getId(), "Network error");
        verify(jobService).logProcessingStep(eq(job1.getId()), eq("RETRY_QUEUED"), anyLong(), eq("QUEUED"), anyString());
        verify(jobService).updateJobStatus(job1.getId(), EmbeddingJobStatus.QUEUED);
    }

    @Test
    void testHandleFailure_DLQRouting() {
        job1.setStatus(EmbeddingJobStatus.IN_PROGRESS);
        job1.setRetryCount(3); // Already 3 retries

        when(jobService.getJobsByStatus(EmbeddingJobStatus.IN_PROGRESS))
                .thenReturn(Collections.singletonList(job1));

        queueManager.handleFailure(job1.getId(), "Fatal database issue");

        verify(jobService).failJob(job1.getId(), "Max retries exceeded. Last error: Fatal database issue");
        verify(jobService).logProcessingStep(eq(job1.getId()), eq("DLQ_ROUTING"), anyLong(), eq("FAILED"), anyString());
    }
}
