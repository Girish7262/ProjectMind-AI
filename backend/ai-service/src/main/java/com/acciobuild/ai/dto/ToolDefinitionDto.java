package com.acciobuild.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping parameter schema and function tool specifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinitionDto {
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private String parameterSchemaJson;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
