package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeVersionDto;
import com.acciobuild.knowledge.security.KnowledgeUserDetails;
import com.acciobuild.knowledge.service.impl.KnowledgeVersionServiceImpl;
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
 * REST Controller exposing version management endpoints.
 */
@RestController
@RequestMapping("/api/v1/knowledge/documents")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeVersionController {

    private final KnowledgeVersionServiceImpl versionService;

    /**
     * Commits a new version state revision for a document.
     */
    @PostMapping("/{documentId}/versions")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    public ResponseEntity<ApiResponse<KnowledgeVersionDto>> createVersion(
            @PathVariable("documentId") UUID documentId,
            @Valid @RequestBody KnowledgeVersionDto dto,
            @AuthenticationPrincipal KnowledgeUserDetails principal) {
        log.info("REST: Committing version revision for document ID: {}", documentId);
        ApiResponse<KnowledgeVersionDto> res = versionService.createVersion(documentId, dto, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
