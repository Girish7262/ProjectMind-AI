package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeRelationshipDto;
import com.acciobuild.knowledge.service.KnowledgeRelationshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing document relationship mapping endpoints.
 */
@RestController
@RequestMapping("/api/v1/knowledge/relationships")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeRelationshipController {

    private final KnowledgeRelationshipService relationshipService;

    /**
     * Connects two documents.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    public ResponseEntity<ApiResponse<KnowledgeRelationshipDto>> linkDocuments(
            @Valid @RequestBody KnowledgeRelationshipDto dto) {
        log.info("REST: Mapping relationship between {} and {}", dto.getSourceDocumentId(), dto.getTargetDocumentId());
        ApiResponse<KnowledgeRelationshipDto> res = relationshipService.linkDocuments(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lists relations originating from a source document.
     */
    @GetMapping("/source/{documentId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR', 'READER')")
    public ResponseEntity<ApiResponse<List<KnowledgeRelationshipDto>>> getRelationships(
            @PathVariable("documentId") UUID documentId) {
        log.info("REST: Fetching relationships for source document ID: {}", documentId);
        ApiResponse<List<KnowledgeRelationshipDto>> res = relationshipService.getRelationships(documentId);
        return ResponseEntity.ok(res);
    }
}
