package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.ConversationDto;
import com.acciobuild.ai.service.ConversationService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST Controller exposing AI Conversation Orchestration endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversations", description = "AI Conversation management endpoints")
@PreAuthorize("isAuthenticated()")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Registers a new conversation session associated with a project.
     */
    @PostMapping
    @Operation(summary = "Create conversation", description = "Registers a new conversation session associated with a project")
    public ResponseEntity<ApiResponse<ConversationDto>> create(@Valid @RequestBody ConversationDto dto) {
        log.info("REST: Request to create conversation for project: {}", dto.getProjectId());
        ConversationDto result = conversationService.createConversation(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Conversation created successfully", result));
    }

    /**
     * Returns active conversations mapped inside a project.
     */
    @GetMapping
    @Operation(summary = "List conversations", description = "Returns active conversations mapped inside a project")
    public ResponseEntity<ApiResponse<List<ConversationDto>>> list(@RequestParam("projectId") UUID projectId) {
        log.info("REST: Request to list conversations for project: {}", projectId);
        List<ConversationDto> list = conversationService.getConversationsByProject(projectId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversations retrieved successfully", list));
    }

    /**
     * Retrieves conversation metadata parameters by ID.
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation details", description = "Retrieves conversation metadata parameters")
    public ResponseEntity<ApiResponse<ConversationDto>> get(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to get conversation details: {}", conversationId);
        ConversationDto dto = conversationService.getConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversation details retrieved", dto));
    }

    /**
     * Renames the title header value of an active conversation.
     */
    @PutMapping("/{conversationId}")
    @Operation(summary = "Rename conversation", description = "Renames the title header value of an active conversation")
    public ResponseEntity<ApiResponse<Void>> rename(@PathVariable("conversationId") UUID conversationId, @RequestParam("title") String title) {
        log.info("REST: Request to rename conversation: {} to '{}'", conversationId, title);
        conversationService.renameConversation(conversationId, title);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversation title updated successfully", null));
    }

    /**
     * Closes and soft-deletes a conversation from display indices.
     */
    @DeleteMapping("/{conversationId}")
    @Operation(summary = "Soft delete conversation", description = "Closes and soft-deletes a conversation from display indices")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to soft delete conversation: {}", conversationId);
        conversationService.softDeleteConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversation soft deleted successfully", null));
    }

    /**
     * Places the conversation into read-only archived indices.
     */
    @PostMapping("/{conversationId}/archive")
    @Operation(summary = "Archive conversation", description = "Places the conversation into read-only archived indices")
    public ResponseEntity<ApiResponse<Void>> archive(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to archive conversation: {}", conversationId);
        conversationService.archiveConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversation archived successfully", null));
    }

    /**
     * Restores a closed/archived conversation back to active.
     */
    @PostMapping("/{conversationId}/restore")
    @Operation(summary = "Restore conversation", description = "Restores a closed/archived conversation back to active")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to restore conversation: {}", conversationId);
        conversationService.restoreConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversation restored successfully", null));
    }
}
