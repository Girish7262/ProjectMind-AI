package com.acciobuild.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload request DTO to create or update a project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload details to create or update a project.")
public class ProjectRequest {

    @NotBlank(message = "Project code identifier is required.")
    @Size(max = 50, message = "Project code must not exceed 50 characters.")
    @Schema(description = "Unique project code identifier.", example = "ai-engine", required = true)
    private String projectCode;

    @NotBlank(message = "Project name identifier is required.")
    @Size(max = 100, message = "Project name must not exceed 100 characters.")
    @Schema(description = "Unique project name identifier inside organization.", example = "AI Translation Engine", required = true)
    private String projectName;

    @Size(max = 100, message = "Display name must not exceed 100 characters.")
    @Schema(description = "Display name of the project.", example = "Translation Engine")
    private String displayName;

    @Size(max = 500, message = "Description details must not exceed 500 characters.")
    @Schema(description = "Description details of the project.")
    private String description;

    @NotBlank(message = "Project visibility is required.")
    @Schema(description = "Project access visibility (PRIVATE, ORGANIZATION, PUBLIC).", example = "PRIVATE", required = true)
    private String visibility;
}
