package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload request DTO to modify a member's role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload details to update a member's role.")
public class UpdateRoleRequest {

    @NotBlank(message = "Member role is required.")
    @Schema(description = "Target role name (e.g. ADMIN, MEMBER, VIEWER).", required = true)
    private String role;
}
