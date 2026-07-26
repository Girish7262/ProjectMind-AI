package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.service.ContextBuilderService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST Controller exposing AI RAG Context aggregation endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/context")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Context Builders", description = "AI Dynamic Prompt Context assembly endpoints")
@PreAuthorize("isAuthenticated()")
public class ContextController {

    private final ContextBuilderService contextBuilderService;

    /**
     * Aggregates project knowledge documents, rankings, and histories to build a RAG context.
     */
    @PostMapping("/build")
    @Operation(summary = "Build conversation context", description = "Aggregates project knowledge documents, rankings, and histories to build a RAG context")
    public ResponseEntity<ApiResponse<ContextDto>> buildContext(@Valid @RequestBody BuildContextRequest request) {
        log.info("REST: Request to compile context for conversation: {}", request.getConversationId());
        ContextDto result = contextBuilderService.buildContext(request.getConversationId(), request.getQueryText());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Context assembled successfully", result));
    }

    /**
     * Retrieves the latest active compiled context of a conversation.
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation context", description = "Retrieves the latest active compiled context of a conversation")
    public ResponseEntity<ApiResponse<ContextDto>> getContext(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to fetch context for conversation: {}", conversationId);
        ContextDto dto = contextBuilderService.getContextByConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Latest compiled context retrieved", dto));
    }

    /**
     * Payload contract to request context assembly.
     */
    @Data
    public static class BuildContextRequest {
        @NotNull(message = "Conversation identifier is required.")
        private UUID conversationId;

        @NotBlank(message = "Query text is required.")
        private String queryText;
    }
}
