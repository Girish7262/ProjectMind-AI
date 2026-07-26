package com.acciobuild.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Data Transfer Object representing project tag details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing project tag details.")
public class ProjectTagDto {

    @Schema(description = "Project tag ID UUID.")
    private UUID id;

    @Schema(description = "Project ID associated with tag.")
    private UUID projectId;

    @NotBlank(message = "Tag name identifier is required.")
    @Size(max = 50, message = "Tag name must not exceed 50 characters.")
    @Schema(description = "Unique tag name identifier.", example = "frontend", required = true)
    private String tagName;

    @Size(max = 10, message = "Color code format must not exceed 10 characters.")
    @Schema(description = "HEX color code representation.", example = "#6366f1")
    private String color;
}
