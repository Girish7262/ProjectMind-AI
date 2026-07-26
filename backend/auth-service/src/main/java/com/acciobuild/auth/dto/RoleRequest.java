package com.acciobuild.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

/**
 * Request payload containing parameters to create or update roles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to create or update roles.")
public class RoleRequest {

    @NotBlank(message = "Role name is required.")
    @Size(max = 50, message = "Role name must not exceed 50 characters.")
    @Schema(description = "Unique name of the role.", example = "DEVELOPER", required = true)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters.")
    @Schema(description = "Description of capabilities of the role.", example = "Software engineering profile mapping platform APIs.")
    private String description;

    @Schema(description = "Active status of the role.", example = "true")
    @Builder.Default
    private boolean status = true;

    @Schema(description = "Set of unique Permission IDs mapping target capability levels.")
    private Set<UUID> permissionIds;
}
