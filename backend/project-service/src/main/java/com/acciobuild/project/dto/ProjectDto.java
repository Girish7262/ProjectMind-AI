package com.acciobuild.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing Project profile metadata details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing project details.")
public class ProjectDto {

    @Schema(description = "Project ID UUID.")
    private UUID id;

    @Schema(description = "Organization ID associated with the project.")
    private UUID organizationId;

    @Schema(description = "Unique project code identifier.", example = "ai-engine")
    private String projectCode;

    @Schema(description = "Unique project name identifier inside organization.", example = "AI Translation Engine")
    private String projectName;

    @Schema(description = "Display name of the project.", example = "Translation Engine")
    private String displayName;

    @Schema(description = "Description details of the project.")
    private String description;

    @Schema(description = "Operational project status (PLANNING, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, DELETED).", example = "ACTIVE")
    private String status;

    @Schema(description = "Project access visibility (PRIVATE, ORGANIZATION, PUBLIC).", example = "PRIVATE")
    private String visibility;

    @Schema(description = "User ID who created the project.")
    private UUID createdBy;

    @Schema(description = "User ID who updated the project.")
    private UUID updatedBy;

    @Schema(description = "Creation timestamp.")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp.")
    private LocalDateTime updatedAt;
}
