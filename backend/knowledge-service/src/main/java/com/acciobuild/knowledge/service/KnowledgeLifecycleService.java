package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import java.util.UUID;

/**
 * Service contract managing document state machine transitions.
 */
public interface KnowledgeLifecycleService {

    /**
     * Transitions state from DRAFT/RESTORED to REVIEW.
     */
    ApiResponse<KnowledgeDocumentDto> submitForReview(UUID documentId);

    /**
     * Transitions state from REVIEW to APPROVED.
     */
    ApiResponse<KnowledgeDocumentDto> approve(UUID documentId);

    /**
     * Transitions state from REVIEW to DRAFT (under rejected change comments).
     */
    ApiResponse<KnowledgeDocumentDto> reject(UUID documentId);

    /**
     * Transitions state from APPROVED to PUBLISHED.
     */
    ApiResponse<KnowledgeDocumentDto> publish(UUID documentId);

    /**
     * Transitions state from PUBLISHED to ARCHIVED.
     */
    ApiResponse<KnowledgeDocumentDto> archive(UUID documentId);

    /**
     * Transitions state from ARCHIVED to RESTORED.
     */
    ApiResponse<KnowledgeDocumentDto> restore(UUID documentId);

    /**
     * Transitions state to DELETED (soft delete).
     */
    ApiResponse<KnowledgeDocumentDto> softDelete(UUID documentId);
}
