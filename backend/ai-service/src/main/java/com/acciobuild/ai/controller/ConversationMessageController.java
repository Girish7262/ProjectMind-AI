package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.MessageDto;
import com.acciobuild.ai.service.ConversationMessageService;
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
 * REST Controller exposing AI Message management endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Messages", description = "AI Conversation messages management endpoints")
@PreAuthorize("isAuthenticated()")
public class ConversationMessageController {

    private final ConversationMessageService messageService;

    /**
     * Appends a new message (User or Assistant role) to the conversation.
     */
    @PostMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Add message", description = "Appends a new message (User or Assistant role) to the conversation history")
    public ResponseEntity<ApiResponse<MessageDto>> addMessage(
            @PathVariable("conversationId") UUID conversationId,
            @Valid @RequestBody MessageDto dto) {
        log.info("REST: Request to add message to conversation: {}", conversationId);
        MessageDto result = messageService.addMessage(conversationId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Message added successfully", result));
    }

    /**
     * Retrieves full ordered chat messages history of a conversation.
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Get conversation messages", description = "Retrieves full ordered chat messages history of a conversation")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(@PathVariable("conversationId") UUID conversationId) {
        log.info("REST: Request to fetch message history for conversation: {}", conversationId);
        List<MessageDto> list = messageService.getMessagesByConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Messages history retrieved successfully", list));
    }

    /**
     * Deletes a specific message by ID reference.
     */
    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete message", description = "Deletes a specific message by ID reference")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable("messageId") UUID messageId) {
        log.info("REST: Request to delete message: {}", messageId);
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Message deleted successfully", null));
    }
}
