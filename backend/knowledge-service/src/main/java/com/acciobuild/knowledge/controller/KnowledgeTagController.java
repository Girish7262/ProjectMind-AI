package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeTagDto;
import com.acciobuild.knowledge.service.KnowledgeTagService;
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
 * REST Controller exposing tag classification endpoints.
 */
@RestController
@RequestMapping("/api/v1/knowledge/tags")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeTagController {

    private final KnowledgeTagService tagService;

    /**
     * Registers a new custom tag.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    public ResponseEntity<ApiResponse<KnowledgeTagDto>> createTag(
            @Valid @RequestBody KnowledgeTagDto dto) {
        log.info("REST: Registering tag '{}'", dto.getName());
        ApiResponse<KnowledgeTagDto> res = tagService.createTag(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lists tags defined inside a specific project.
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR', 'READER')")
    public ResponseEntity<ApiResponse<List<KnowledgeTagDto>>> getTags(
            @PathVariable("projectId") UUID projectId) {
        log.info("REST: Fetching tags for project ID: {}", projectId);
        ApiResponse<List<KnowledgeTagDto>> res = tagService.getTags(projectId);
        return ResponseEntity.ok(res);
    }
}
