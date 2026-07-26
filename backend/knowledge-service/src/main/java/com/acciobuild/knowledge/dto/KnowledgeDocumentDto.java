package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload DTO representing a Knowledge Document.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocumentDto {
    private UUID id;
    private UUID projectId;
    private UUID organizationId;
    private String title;
    private String slug;
    private String summary;
    private String contentType;
    private String contentFormat;
    private String status;
    private String visibility;
    private String sourceType;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
