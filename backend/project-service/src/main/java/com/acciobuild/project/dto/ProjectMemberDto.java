package com.acciobuild.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing project member details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing project member details.")
public class ProjectMemberDto {

    @Schema(description = "Project member mapping ID UUID.")
    private UUID id;

    @Schema(description = "Project ID associated with member.")
    private UUID projectId;

    @Schema(description = "User ID enrolled as collaborator.")
    private UUID userId;

    @Schema(description = "Project member access role (MAINTAINER, DEVELOPER, VIEWER).", example = "DEVELOPER")
    private String role;

    @Schema(description = "Joined timestamp.")
    private LocalDateTime joinedAt;

    @Schema(description = "Enrolled member status.", example = "ACTIVE")
    private String status;
}
