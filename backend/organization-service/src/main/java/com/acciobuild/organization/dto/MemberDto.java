package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing membership details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO representing membership details.")
public class MemberDto {

    @Schema(description = "Unique Identifier of the membership.")
    private UUID id;

    @Schema(description = "Organization ID associated with member.")
    private UUID organizationId;

    @Schema(description = "User ID reference.")
    private UUID userId;

    @Schema(description = "Assigned role code (e.g. OWNER, ADMIN, MEMBER).")
    private String role;

    @Schema(description = "Joined timestamp.")
    private LocalDateTime joinedAt;

    @Schema(description = "Enrollment status state (e.g. ACTIVE, BLOCKED).")
    private String status;
}
