package com.acciobuild.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

/**
 * Response payload returning user profile mapping active roles listings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload returning user and their assigned roles.")
public class UserRolesResponse {

    @Schema(description = "Unique Identifier of the User.")
    private UUID userId;

    @Schema(description = "Registered email address of the User.")
    private String email;

    @Schema(description = "Roles assigned to the User.")
    private Set<RoleResponse> roles;
}
