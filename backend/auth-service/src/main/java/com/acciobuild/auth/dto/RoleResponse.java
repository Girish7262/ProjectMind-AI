package com.acciobuild.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

/**
 * Response payload representing role details and its permissions collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload representing role details.")
public class RoleResponse {

    @Schema(description = "Unique identifier of the role.")
    private UUID id;

    @Schema(description = "Unique name of the role.")
    private String name;

    @Schema(description = "Description of capabilities of the role.")
    private String description;

    @Schema(description = "Active status of the role.")
    private boolean status;

    @Schema(description = "Set of permissions associated with the role.")
    private Set<PermissionResponse> permissions;
}
