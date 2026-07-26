package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.knowledge.domain.event.KnowledgeVersionCreatedEvent;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeVersion;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.dto.KnowledgeVersionDto;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
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
 * Service implementation managing Knowledge Document versions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeVersionServiceImpl {

    private final KnowledgeDocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Commits a new immutable version revision state for a document.
     */
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeVersionDto> createVersion(UUID documentId, KnowledgeVersionDto dto, UUID creatorId) {
        log.info("Creating version for document ID: {}", documentId);

        KnowledgeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Document not found for ID: " + documentId));

        if (doc.getStatus() == KnowledgeStatus.ARCHIVED || doc.getStatus() == KnowledgeStatus.DELETED) {
            throw new InvalidKnowledgeOperationException("Archived or deleted documents are read-only.");
        }

        // Calculate next version number
        int nextNum = doc.getVersions().stream()
                .mapToInt(KnowledgeVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;

        KnowledgeVersion version = new KnowledgeVersion();
        version.setId(UUID.randomUUID());
        version.setDocument(doc);
        version.setVersionNumber(nextNum);
        version.setContentHash(dto.getContentHash());
        version.setStorageLocation(dto.getStorageLocation());
        version.setChangeSummary(dto.getChangeSummary());
        version.setCreatedBy(creatorId);
        version.setCreatedAt(LocalDateTime.now());

        doc.getVersions().add(version);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        eventPublisher.publishEvent(new KnowledgeVersionCreatedEvent(
                doc.getOrganizationId(), documentId, nextNum, MdcHelper.getCorrelationId()));

        return ApiResponse.<KnowledgeVersionDto>builder()
                .status(201)
                .message("Version committed successfully.")
                .data(KnowledgeVersionDto.builder()
                        .id(version.getId())
                        .documentId(documentId)
                        .versionNumber(nextNum)
                        .contentHash(version.getContentHash())
                        .storageLocation(version.getStorageLocation())
                        .changeSummary(version.getChangeSummary())
                        .createdAt(version.getCreatedAt())
                        .createdBy(version.getCreatedBy())
                        .build())
                .build();
    }
}
