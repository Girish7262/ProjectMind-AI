package com.acciobuild.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Response payload representing permission details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload representing permission details.")
public class PermissionResponse {

    @Schema(description = "Unique Identifier of the permission.")
    private UUID id;

    @Schema(description = "Display name of the permission.")
    private String name;

    @Schema(description = "Permission code used in authorization checks.")
    private String code;

    @Schema(description = "Description of capability access granted.")
    private String description;

    @Schema(description = "Functional module scope group.")
    private String module;
}
