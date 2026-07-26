package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload DTO representing a Knowledge Metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeMetadataDto {
    private UUID documentId;
    private String language;
    private String keywords;
    private String author;
    private String reviewStatus;
    private String approvalStatus;
    private LocalDateTime lastReviewedAt;
}
