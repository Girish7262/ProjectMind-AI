package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.CitationDto;
import com.acciobuild.ai.service.CitationService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing Citation annotation endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/messages/{messageId}/citations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Citations", description = "AI Generated response citations annotation endpoints")
@PreAuthorize("isAuthenticated()")
public class CitationController {

    private final CitationService citationService;

    /**
     * Lists references/citations attached to a message.
     */
    @GetMapping
    @Operation(summary = "Get citations", description = "Lists references/citations attached to a message")
    public ResponseEntity<ApiResponse<List<CitationDto>>> getCitations(@PathVariable("messageId") UUID messageId) {
        log.info("REST: Request to get citations for message: {}", messageId);
        List<CitationDto> list = citationService.getCitationsForMessage(messageId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Citations retrieved successfully", list));
    }

    /**
     * Attaches source citations to a response message.
     */
    @PostMapping
    @Operation(summary = "Record citations", description = "Attaches source citations to a response message")
    public ResponseEntity<ApiResponse<Void>> recordCitations(
            @PathVariable("messageId") UUID messageId,
            @Valid @RequestBody List<CitationDto> citations) {
        log.info("REST: Request to record citations for message: {}", messageId);
        citationService.recordCitations(messageId, citations);
        return ResponseEntity.ok(new ApiResponse<>(200, "Citations recorded successfully", null));
    }
}
