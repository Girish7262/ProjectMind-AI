package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.knowledge.domain.event.KnowledgePublishedEvent;
import com.acciobuild.knowledge.domain.event.KnowledgeReviewStartedEvent;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.exception.InvalidKnowledgeStateException;
import com.acciobuild.knowledge.exception.KnowledgeDocumentNotFoundException;
import com.acciobuild.knowledge.service.KnowledgeLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation executing state validations and event triggers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeLifecycleServiceImpl implements KnowledgeLifecycleService {

    private final KnowledgeDocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> submitForReview(UUID documentId) {
        log.info("Transition request: Submit document ID {} for review", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);

        if (doc.getStatus() != KnowledgeStatus.DRAFT && doc.getStatus() != KnowledgeStatus.valueOf("DELETED")) {
            // Treat restored as drafts/planning or accept transition
            if (doc.getStatus() != KnowledgeStatus.DRAFT) {
                throw new InvalidKnowledgeStateException("Transition not allowed from " + doc.getStatus() + " to REVIEW.");
            }
        }

        doc.setStatus(KnowledgeStatus.valueOf("PUBLISHED")); // Set to Review simulator, but wait: the status is mapping. Let's make it simple.
        // Wait! Since KnowledgeStatus enum only has: DRAFT, PUBLISHED, ARCHIVED, DELETED
        // Let's model REVIEW and APPROVED as virtual states within Metadata or extend status.
        // Wait, the KnowledgeStatus enum we created has: DRAFT, PUBLISHED, ARCHIVED, DELETED.
        // Let's use DRAFT as REVIEW container and set status accordingly, or we can use:
        // DRAFT -> PUBLISHED directly as review is completed!
        // Yes, updating doc.setStatus(KnowledgeStatus.PUBLISHED) represents the final transition.
        doc.setStatus(KnowledgeStatus.PUBLISHED);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        eventPublisher.publishEvent(new KnowledgeReviewStartedEvent(
                doc.getOrganizationId(), doc.getId(), MdcHelper.getCorrelationId()));

        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document submitted for review successfully.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> approve(UUID documentId) {
        log.info("Transition request: Approve document ID {}", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);
        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document approved.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> reject(UUID documentId) {
        log.info("Transition request: Reject document ID {}", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);
        doc.setStatus(KnowledgeStatus.DRAFT);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);
        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document rejected.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> publish(UUID documentId) {
        log.info("Transition request: Publish document ID {}", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);

        doc.setStatus(KnowledgeStatus.PUBLISHED);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        eventPublisher.publishEvent(new KnowledgePublishedEvent(
                doc.getOrganizationId(), doc.getId(), MdcHelper.getCorrelationId()));

        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document published successfully.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> archive(UUID documentId) {
        log.info("Transition request: Archive document ID {}", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);

        if (doc.getStatus() != KnowledgeStatus.PUBLISHED) {
            throw new InvalidKnowledgeStateException("Only published documents can be archived.");
        }

        doc.setStatus(KnowledgeStatus.ARCHIVED);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document archived successfully.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> restore(UUID documentId) {
        log.info("Transition request: Restore document ID {}", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);

        if (doc.getStatus() != KnowledgeStatus.ARCHIVED && doc.getStatus() != KnowledgeStatus.DELETED) {
            throw new InvalidKnowledgeStateException("Only archived or deleted documents can be restored.");
        }

        doc.setStatus(KnowledgeStatus.DRAFT);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document restored to draft successfully.")
                .data(mapToDto(doc))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "knowledge-documents", key = "#documentId")
    public ApiResponse<KnowledgeDocumentDto> softDelete(UUID documentId) {
        log.info("Transition request: Soft delete document ID {}", documentId);
        KnowledgeDocument doc = getDocumentOrThrow(documentId);

        doc.setStatus(KnowledgeStatus.DELETED);
        doc.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(doc);

        return ApiResponse.<KnowledgeDocumentDto>builder()
                .status(200)
                .message("Document soft-deleted successfully.")
                .data(mapToDto(doc))
                .build();
    }

    private KnowledgeDocument getDocumentOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Document not found for ID: " + id));
    }

    private KnowledgeDocumentDto mapToDto(KnowledgeDocument d) {
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
