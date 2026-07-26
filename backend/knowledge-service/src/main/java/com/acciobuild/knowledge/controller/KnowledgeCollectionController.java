package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeCollectionDto;
import com.acciobuild.knowledge.service.KnowledgeCollectionService;
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
 * REST Controller exposing document collection endpoints.
 */
@RestController
@RequestMapping("/api/v1/knowledge/collections")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeCollectionController {

    private final KnowledgeCollectionService collectionService;

    /**
     * Registers a new collection.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    public ResponseEntity<ApiResponse<KnowledgeCollectionDto>> createCollection(
            @Valid @RequestBody KnowledgeCollectionDto dto) {
        log.info("REST: Registering collection '{}'", dto.getName());
        ApiResponse<KnowledgeCollectionDto> res = collectionService.createCollection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lists collections registered inside a specific project.
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR', 'READER')")
    public ResponseEntity<ApiResponse<List<KnowledgeCollectionDto>>> getCollections(
            @PathVariable("projectId") UUID projectId) {
        log.info("REST: Fetching collections for project ID: {}", projectId);
        ApiResponse<List<KnowledgeCollectionDto>> res = collectionService.getCollections(projectId);
        return ResponseEntity.ok(res);
    }
}
