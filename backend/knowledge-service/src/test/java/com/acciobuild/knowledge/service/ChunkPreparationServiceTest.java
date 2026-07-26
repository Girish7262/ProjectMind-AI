package com.acciobuild.knowledge.service;

import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.domain.repository.DocumentIndexMetadataRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeSearchIndexRepository;
import com.acciobuild.knowledge.enums.KnowledgeSourceType;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import com.acciobuild.knowledge.service.impl.ChunkPreparationServiceImpl;
import com.acciobuild.knowledge.service.impl.IndexPreparationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating document chunk partitioning and index preparations.
 */
@ExtendWith(MockitoExtension.class)
public class ChunkPreparationServiceTest {

    @Mock private KnowledgeDocumentRepository documentRepository;
    @Mock private KnowledgeDocumentChunkRepository chunkRepository;
    @Mock private DocumentIndexMetadataRepository metadataRepository;
    @Mock private KnowledgeSearchIndexRepository searchIndexRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ChunkPreparationServiceImpl chunkService;
    @InjectMocks private IndexPreparationServiceImpl indexService;

    @Test
    void testPrepareChunks_Success() {
        UUID docId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setOrganizationId(UUID.randomUUID());
        doc.setStatus(KnowledgeStatus.DRAFT);
        doc.setVisibility(KnowledgeVisibility.INTERNAL);
        doc.setSourceType(KnowledgeSourceType.MANUAL);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // Content of 700 chars (will result in 2 chunks of size 500 with overlap 100)
        String content = "A".repeat(700);

        chunkService.prepareChunks(docId, content);

        ArgumentCaptor<KnowledgeDocumentChunk> chunkCaptor = ArgumentCaptor.forClass(KnowledgeDocumentChunk.class);
        verify(chunkRepository, atLeastOnce()).save(chunkCaptor.capture());
        verify(metadataRepository).save(any());

        List<KnowledgeDocumentChunk> savedChunks = chunkCaptor.getAllValues();
        assertEquals(2, savedChunks.size());
        for (KnowledgeDocumentChunk chunk : savedChunks) {
            assertEquals("en", chunk.getLanguage());
            assertEquals(0.0, chunk.getEstimatedCost());
            assertEquals(0, chunk.getPriority());
            assertTrue(chunk.isEmbeddingEligibility());
            assertEquals("PENDING", chunk.getProcessingStatus());
            assertNotNull(chunk.getChunkHash());
            assertEquals(64, chunk.getChunkHash().length());
            assertEquals(chunk.getChunkHash(), chunk.getContentChecksum());
        }
    }

    @Test
    void testBuildIndex_Success() {
        UUID docId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setTitle("AI Continuity Guide");
        doc.setSummary("A simple summary of continuity.");
        doc.setOrganizationId(UUID.randomUUID());

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        indexService.buildIndex(docId);

        verify(searchIndexRepository).save(any());
    }
}
