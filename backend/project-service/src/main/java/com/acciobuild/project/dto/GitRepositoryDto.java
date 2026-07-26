package com.acciobuild.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping Git Repository parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepositoryDto {
    private UUID id;
    private UUID projectId;
    private String provider;
    private String repositoryName;
    private String repositoryUrl;
    private String defaultBranch;
    private String status;
    private String visibility;
    private boolean isArchived;
    private LocalDateTime lastSyncedAt;
}
