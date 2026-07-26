package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.PromptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object mapping prompt templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateDto {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private PromptStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PromptVersionDto> versions;
}
