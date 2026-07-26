package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Payload request DTO to enroll a user member directly to an organization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload details to add a member to an organization.")
public class AddMemberRequest {

    @NotNull(message = "User ID reference is required.")
    @Schema(description = "Target user ID UUID.", required = true)
    private UUID userId;

    @NotBlank(message = "Member role is required.")
    @Schema(description = "Target role (e.g. ADMIN, MEMBER, VIEWER).", required = true)
    private String role;
}
