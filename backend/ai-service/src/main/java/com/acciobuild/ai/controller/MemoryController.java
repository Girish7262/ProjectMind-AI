package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.MemoryDto;
import com.acciobuild.ai.service.MemoryService;
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
 * REST Controller exposing Conversation Memory variables endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/conversations/{conversationId}/memory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Memories", description = "AI Conversation memory tracking variables endpoints")
@PreAuthorize("isAuthenticated()")
public class MemoryController {

    private final MemoryService memoryService;

    /**
     * Lists memory state variables parsed from conversation context.
     */
    @GetMapping
    @Operation(summary = "Get memory variables", description = "Lists memory state variables parsed from conversation context")
    public ResponseEntity<ApiResponse<List<MemoryDto>>> getMemory(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to get memory variables for conversation: {}", conversationId);
        List<MemoryDto> list = memoryService.getMemoryForConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Memory variables retrieved successfully", list));
    }

    /**
     * Updates or appends a key-value memory context payload.
     */
    @PostMapping
    @Operation(summary = "Update memory variable", description = "Updates or appends a key-value memory context payload")
    public ResponseEntity<ApiResponse<Void>> updateMemory(
            @PathVariable("conversationId") UUID conversationId,
            @Valid @RequestBody MemoryDto dto) {
        log.info("REST: Request to update memory for conversation: {}", conversationId);
        memoryService.updateMemory(conversationId, dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Memory context updated successfully", null));
    }

    /**
     * Clears all memory variables of a conversation.
     */
    @DeleteMapping
    @Operation(summary = "Clear memory context", description = "Clears all memory variables of a conversation")
    public ResponseEntity<ApiResponse<Void>> clearMemory(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to clear memory for conversation: {}", conversationId);
        memoryService.clearMemory(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Conversation memory cleared successfully", null));
    }
}
