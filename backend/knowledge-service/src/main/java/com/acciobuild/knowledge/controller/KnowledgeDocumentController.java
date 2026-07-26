package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.security.KnowledgeUserDetails;
import com.acciobuild.knowledge.service.KnowledgeDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST Controller exposing document actions.
 */
@RestController
@RequestMapping("/api/v1/knowledge/documents")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;

    /**
     * Provisions a new Knowledge Document.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentDto>> createDocument(
            @Valid @RequestBody KnowledgeDocumentDto dto,
            @AuthenticationPrincipal KnowledgeUserDetails principal) {
        log.info("REST: Request to create document '{}'", dto.getTitle());
        ApiResponse<KnowledgeDocumentDto> res = documentService.createDocument(dto, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Retrieves a Knowledge Document by ID.
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR', 'READER')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentDto>> getDocument(
            @PathVariable("documentId") UUID documentId) {
        log.info("REST: Request to get document ID '{}'", documentId);
        ApiResponse<KnowledgeDocumentDto> res = documentService.getDocument(documentId);
        return ResponseEntity.ok(res);
    }

    /**
     * Retrieves a Knowledge Document by project ID and unique slug.
     */
    @GetMapping("/project/{projectId}/slug/{slug}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR', 'READER')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentDto>> getDocumentBySlug(
            @PathVariable("projectId") UUID projectId,
            @PathVariable("slug") String slug) {
        log.info("REST: Request to get document by slug '{}' in project '{}'", slug, projectId);
        ApiResponse<KnowledgeDocumentDto> res = documentService.getDocumentBySlug(projectId, slug);
        return ResponseEntity.ok(res);
    }
}
