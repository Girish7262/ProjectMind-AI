package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.exception.InvalidKnowledgeStateException;
import com.acciobuild.knowledge.service.impl.KnowledgeLifecycleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Knowledge Document lifecycle state machine transitions.
 */
@ExtendWith(MockitoExtension.class)
public class KnowledgeLifecycleTest {

    @Mock private KnowledgeDocumentRepository documentRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private KnowledgeLifecycleServiceImpl lifecycleService;

    @Test
    void testArchiveDocument_Success() {
        UUID docId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setStatus(KnowledgeStatus.PUBLISHED);
        doc.setOrganizationId(UUID.randomUUID());

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(KnowledgeDocument.class))).thenAnswer(i -> i.getArgument(0));

        ApiResponse<KnowledgeDocumentDto> res = lifecycleService.archive(docId);

        assertNotNull(res);
        assertEquals("ARCHIVED", res.getData().getStatus());
    }

    @Test
    void testArchiveDocument_InvalidState_Throws() {
        UUID docId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setStatus(KnowledgeStatus.DRAFT);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        assertThrows(InvalidKnowledgeStateException.class, () -> {
            lifecycleService.archive(docId);
        });
    }

    @Test
    void testRestoreDocument_FromArchived_Success() {
        UUID docId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setStatus(KnowledgeStatus.ARCHIVED);
        doc.setOrganizationId(UUID.randomUUID());

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(KnowledgeDocument.class))).thenAnswer(i -> i.getArgument(0));

        ApiResponse<KnowledgeDocumentDto> res = lifecycleService.restore(docId);

        assertNotNull(res);
        assertEquals("DRAFT", res.getData().getStatus());
    }
}
