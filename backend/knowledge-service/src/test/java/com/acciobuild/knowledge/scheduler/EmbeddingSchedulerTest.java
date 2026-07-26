package com.acciobuild.knowledge.scheduler;

import com.acciobuild.knowledge.ai.EmbeddingClient;
import com.acciobuild.knowledge.ai.VectorStoreAdapter;
import com.acciobuild.knowledge.domain.model.EmbeddingJob;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.queue.EmbeddingQueueManager;
import com.acciobuild.knowledge.service.EmbeddingJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

/**
 * Unit test validating background embedding job execution, vector storage routing, and failure handling.
 */
@ExtendWith(MockitoExtension.class)
public class EmbeddingSchedulerTest {

    @Mock
    private EmbeddingQueueManager queueManager;
    @Mock
    private EmbeddingJobService jobService;
    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private VectorStoreAdapter vectorStoreAdapter;
    @Mock
    private com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository chunkRepository;

    @InjectMocks
    private EmbeddingJobScheduler scheduler;

    private EmbeddingJob job;
    private List<KnowledgeDocumentChunk> chunks;

    @BeforeEach
    void setUp() {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(UUID.randomUUID());
        doc.setOrganizationId(UUID.randomUUID());
        KnowledgeDocumentChunk chunk = new KnowledgeDocumentChunk();
        chunk.setId(UUID.randomUUID());
        chunk.setContent("Hello World");
        chunk.setDocument(doc);
        chunks = Collections.singletonList(chunk);

        job = new EmbeddingJob();
        job.setId(UUID.randomUUID());
        job.setDocument(doc);
        job.setEstimatedCost(0.01);
    }

    @Test
    void testScheduler_ProcessNextJob_Success() {
        when(queueManager.poll()).thenReturn(job);
        when(chunkRepository.findByDocumentIdOrderByChunkIndexAsc(any())).thenReturn(chunks);
        List<Double> vector = Collections.singletonList(0.5);
        when(embeddingClient.embedBatch(anyList())).thenReturn(Collections.singletonList(vector));

        scheduler.processNextJob();

        verify(jobService).logProcessingStep(eq(job.getId()), eq("STARTED"), anyLong(), eq("IN_PROGRESS"), anyString());
        verify(embeddingClient).embedBatch(anyList());
        verify(vectorStoreAdapter).saveEmbeddings(eq(job.getDocument().getId()), anyList(), anyList());
        verify(jobService).completeJob(job.getId(), job.getEstimatedCost());
    }

    @Test
    void testScheduler_ProcessNextJob_Failure() {
        when(queueManager.poll()).thenReturn(job);
        when(chunkRepository.findByDocumentIdOrderByChunkIndexAsc(any())).thenReturn(chunks);
        when(embeddingClient.embedBatch(anyList())).thenThrow(new RuntimeException("API error"));

        scheduler.processNextJob();

        verify(jobService).logProcessingStep(eq(job.getId()), eq("JOB_FAILED"), anyLong(), eq("FAILED"), eq("API error"));
        verify(queueManager).handleFailure(job.getId(), "API error");
    }
}
