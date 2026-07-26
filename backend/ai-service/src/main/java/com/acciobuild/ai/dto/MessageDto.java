package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object mapping content and generation metrics of messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private UUID id;
    private UUID organizationId;
    private UUID conversationId;
    private MessageRole role;
    private String content;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long responseDurationMs;
    private LocalDateTime createdAt;
    private List<CitationDto> citations;
}
