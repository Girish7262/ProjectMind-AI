package com.acciobuild.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Payload request DTO to add a collaborator member to a project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload details to add a member to a project.")
public class AddMemberRequest {

    @NotNull(message = "User ID reference is required.")
    @Schema(description = "Target user ID UUID.", required = true)
    private UUID userId;

    @NotBlank(message = "Member role is required.")
    @Schema(description = "Target role (MAINTAINER, DEVELOPER, VIEWER).", required = true)
    private String role;
}
