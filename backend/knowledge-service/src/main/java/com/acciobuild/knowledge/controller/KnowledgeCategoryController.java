package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeCategoryDto;
import com.acciobuild.knowledge.service.KnowledgeCategoryService;
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
 * REST Controller exposing category classification endpoints.
 */
@RestController
@RequestMapping("/api/v1/knowledge/categories")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeCategoryController {

    private final KnowledgeCategoryService categoryService;

    /**
     * Registers a new category.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    public ResponseEntity<ApiResponse<KnowledgeCategoryDto>> createCategory(
            @Valid @RequestBody KnowledgeCategoryDto dto) {
        log.info("REST: Registering category '{}'", dto.getName());
        ApiResponse<KnowledgeCategoryDto> res = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * Lists categories defined inside a specific project.
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR', 'READER')")
    public ResponseEntity<ApiResponse<List<KnowledgeCategoryDto>>> getCategories(
            @PathVariable("projectId") UUID projectId) {
        log.info("REST: Fetching categories for project ID: {}", projectId);
        ApiResponse<List<KnowledgeCategoryDto>> res = categoryService.getCategories(projectId);
        return ResponseEntity.ok(res);
    }
}
