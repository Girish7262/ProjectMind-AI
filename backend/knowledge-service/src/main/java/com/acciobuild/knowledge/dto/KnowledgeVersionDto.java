package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload DTO representing a Knowledge Version.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeVersionDto {
    private UUID id;
    private UUID documentId;
    private int versionNumber;
    private String contentHash;
    private String storageLocation;
    private String changeSummary;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
