package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.knowledge.client.ProjectServiceClient;
import com.acciobuild.knowledge.domain.event.KnowledgeDocumentCreatedEvent;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeMetadata;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.dto.KnowledgeVersionDto;
import com.acciobuild.knowledge.dto.ProjectDto;
import com.acciobuild.knowledge.enums.ApprovalStatus;
import com.acciobuild.knowledge.enums.KnowledgeSourceType;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import com.acciobuild.knowledge.enums.ReviewStatus;
import com.acciobuild.knowledge.exception.DuplicateKnowledgeDocumentException;
import com.acciobuild.knowledge.exception.InvalidKnowledgeOperationException;
import com.acciobuild.knowledge.exception.KnowledgeDocumentNotFoundException;
import com.acciobuild.knowledge.service.KnowledgeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation managing Knowledge Document lifecycles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final ProjectServiceClient projectServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeDocumentDto> createDocument(KnowledgeDocumentDto dto, UUID creatorId) {
        log.info("Provisioning knowledge document: {} inside project ID: {}", dto.getTitle(), dto.getProjectId());

        // Validate Project exists and is active
        try {
            ApiResponse<ProjectDto> projRes = projectServiceClient.getProjectById(dto.getProjectId());
            if (projRes == null || projRes.getData() == null || !"ACTIVE".equals(projRes.getData().getStatus())) {
                throw new InvalidKnowledgeOperationException("Associated workspace project is not active.");
            }
            // Set organization context from remote metadata
            dto.setOrganizationId(projRes.getData().getOrganizationId());
        } catch (InvalidKnowledgeOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Feign validation failed for project ID: {}", dto.getProjectId(), e);
            throw new InvalidKnowledgeOperationException("Could not validate project context from Project Service: " + e.getMessage());
        }

        // Validate Slug uniqueness in project
        if (documentRepository.existsByProjectIdAndSlug(dto.getProjectId(), dto.getSlug())) {
            throw new DuplicateKnowledgeDocumentException("Slug '" + dto.getSlug() + "' already exists inside this project.");
        }

        UUID docId = UUID.randomUUID();
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(docId);
        doc.setProjectId(dto.getProjectId());
        doc.setOrganizationId(dto.getOrganizationId());
        doc.setTitle(dto.getTitle());
        doc.setSlug(dto.getSlug());
        doc.setSummary(dto.getSummary());
        doc.setContentType(dto.getContentType());
        doc.setContentFormat(dto.getContentFormat() != null ? dto.getContentFormat() : "markdown");
        doc.setStatus(KnowledgeStatus.DRAFT);
        doc.setVisibility(KnowledgeVisibility.valueOf(dto.getVisibility().toUpperCase().trim()));
        doc.setSourceType(KnowledgeSourceType.valueOf(dto.getSourceType().toUpperCase().trim()));
        doc.setCreatedBy(creatorId);
        doc.setUpdatedBy(creatorId);

        // Bind default metadata
        KnowledgeMetadata meta = new KnowledgeMetadata();
        meta.setDocument(doc);
        meta.setLanguage("en");
        meta.setReviewStatus(ReviewStatus.PENDING);
        meta.setApprovalStatus(ApprovalStatus.PENDING);
        doc.setMetadata(meta);

        KnowledgeDocument saved = documentRepository.save(doc);

        eventPublisher.publishEvent(new KnowledgeDocumentCreatedEvent(
                doc.getOrganizationId(), docId, saved.getSlug(), MdcHelper.getCorrelationId()));

        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(201)
                .message("Document provisioned successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<KnowledgeDocumentDto> getDocument(UUID id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Document not found for ID: " + id));
        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document fetched.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<KnowledgeDocumentDto> getDocumentBySlug(UUID projectId, String slug) {
        KnowledgeDocument doc = documentRepository.findByProjectIdAndSlug(projectId, slug)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Document not found."));
        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document fetched.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeVersionDto> createVersion(UUID documentId, KnowledgeVersionDto dto, UUID creatorId) {
        // Handled in KnowledgeVersionServiceImpl
        return null;
    }

    private KnowledgeDocumentDto mapToDto(KnowledgeDocument d) {
        if (d == null) return null;
        return KnowledgeDocumentDto.builder()
                .id(d.getId())
                .projectId(d.getProjectId())
                .organizationId(d.getOrganizationId())
                .title(d.getTitle())
                .slug(d.getSlug())
                .summary(d.getSummary())
                .contentType(d.getContentType())
                .contentFormat(d.getContentFormat())
                .status(d.getStatus().name())
                .visibility(d.getVisibility().name())
                .sourceType(d.getSourceType().name())
                .createdBy(d.getCreatedBy())
                .updatedBy(d.getUpdatedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
