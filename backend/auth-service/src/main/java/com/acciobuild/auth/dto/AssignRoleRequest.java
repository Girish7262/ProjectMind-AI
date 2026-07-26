package com.acciobuild.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

/**
 * Request payload containing list of roles to assign or replace for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to assign or replace roles for a user.")
public class AssignRoleRequest {

    @NotEmpty(message = "Role names set is required and cannot be empty.")
    @Schema(description = "Set of Role names (e.g. ORG_ADMIN, DEVELOPER).", example = "[\"DEVELOPER\"]", required = true)
    private Set<String> roleNames;
}
