package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.client.ProjectServiceClient;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeRelationshipRepository;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.dto.KnowledgeRelationshipDto;
import com.acciobuild.knowledge.dto.ProjectDto;
import com.acciobuild.knowledge.enums.KnowledgeSourceType;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import com.acciobuild.knowledge.enums.RelationshipType;
import com.acciobuild.knowledge.exception.DuplicateKnowledgeDocumentException;
import com.acciobuild.knowledge.exception.InvalidKnowledgeOperationException;
import com.acciobuild.knowledge.exception.KnowledgeRelationshipException;
import com.acciobuild.knowledge.service.impl.KnowledgeDocumentServiceImpl;
import com.acciobuild.knowledge.service.impl.KnowledgeRelationshipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Knowledge Service business rules and client integration logic.
 */
@ExtendWith(MockitoExtension.class)
public class KnowledgeDocumentServiceTest {

    @Mock private KnowledgeDocumentRepository documentRepository;
    @Mock private KnowledgeRelationshipRepository relationshipRepository;
    @Mock private ProjectServiceClient projectServiceClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private KnowledgeDocumentServiceImpl documentService;
    @InjectMocks private KnowledgeRelationshipServiceImpl relationshipService;

    private UUID projectId;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        orgId = UUID.randomUUID();
    }

    @Test
    void testCreateDocument_Success() {
        KnowledgeDocumentDto dto = KnowledgeDocumentDto.builder()
                .projectId(projectId)
                .title("Deployment Guide")
                .slug("deploy-guide")
                .visibility("INTERNAL")
                .sourceType("MANUAL")
                .build();

        ProjectDto proj = new ProjectDto(projectId, orgId, "AB-123", "AccioProj", "ACTIVE", "INTERNAL");
        when(projectServiceClient.getProjectById(projectId)).thenReturn(new ApiResponse<>(200, "OK", proj));
        when(documentRepository.existsByProjectIdAndSlug(projectId, "deploy-guide")).thenReturn(false);
        when(documentRepository.save(any(KnowledgeDocument.class))).thenAnswer(i -> i.getArgument(0));

        ApiResponse<KnowledgeDocumentDto> res = documentService.createDocument(dto, UUID.randomUUID());

        assertNotNull(res);
        assertEquals(201, res.getStatus());
        assertEquals("deploy-guide", res.getData().getSlug());
    }

    @Test
    void testCreateDocument_ProjectNotActive_Throws() {
        KnowledgeDocumentDto dto = KnowledgeDocumentDto.builder()
                .projectId(projectId)
                .title("Deployment Guide")
                .slug("deploy-guide")
                .visibility("INTERNAL")
                .sourceType("MANUAL")
                .build();

        ProjectDto proj = new ProjectDto(projectId, orgId, "AB-123", "AccioProj", "ARCHIVED", "INTERNAL");
        when(projectServiceClient.getProjectById(projectId)).thenReturn(new ApiResponse<>(200, "OK", proj));

        assertThrows(InvalidKnowledgeOperationException.class, () -> {
            documentService.createDocument(dto, UUID.randomUUID());
        });
    }

    @Test
    void testCreateDocument_DuplicateSlug_Throws() {
        KnowledgeDocumentDto dto = KnowledgeDocumentDto.builder()
                .projectId(projectId)
                .title("Deployment Guide")
                .slug("deploy-guide")
                .visibility("INTERNAL")
                .sourceType("MANUAL")
                .build();

        ProjectDto proj = new ProjectDto(projectId, orgId, "AB-123", "AccioProj", "ACTIVE", "INTERNAL");
        when(projectServiceClient.getProjectById(projectId)).thenReturn(new ApiResponse<>(200, "OK", proj));
        when(documentRepository.existsByProjectIdAndSlug(projectId, "deploy-guide")).thenReturn(true);

        assertThrows(DuplicateKnowledgeDocumentException.class, () -> {
            documentService.createDocument(dto, UUID.randomUUID());
        });
    }

    @Test
    void testCreateRelationship_SelfLinking_Throws() {
        UUID docId = UUID.randomUUID();
        KnowledgeRelationshipDto dto = new KnowledgeRelationshipDto(null, docId, docId, "REFERENCES", 1.0);

        assertThrows(KnowledgeRelationshipException.class, () -> {
            relationshipService.linkDocuments(dto);
        });
    }
}
