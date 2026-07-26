package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.MemoryScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping summarized context variables in conversations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryDto {
    private UUID id;
    private UUID organizationId;
    private UUID conversationId;
    private MemoryScope memoryScope;
    private String memoryKey;
    private String memoryValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
