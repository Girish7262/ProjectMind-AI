package com.acciobuild.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping snapshot revisions of templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptVersionDto {
    private UUID id;
    private UUID organizationId;
    private UUID templateId;
    private Integer versionNumber;
    private String systemInstruction;
    private String userTemplate;
    private String parametersJson;
    private Boolean isActive;
    private UUID createdBy;
    private LocalDateTime createdAt;
}
