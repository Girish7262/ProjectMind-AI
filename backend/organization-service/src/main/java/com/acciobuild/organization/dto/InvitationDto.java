package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing invitation details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO representing invitation details.")
public class InvitationDto {

    @Schema(description = "Unique Identifier of the invitation.")
    private UUID id;

    @Schema(description = "Organization ID associated with invitation.")
    private UUID organizationId;

    @Schema(description = "Invitee email address.")
    private String email;

    @Schema(description = "Secure invite token value.")
    private String inviteToken;

    @Schema(description = "Expiration timestamp.")
    private LocalDateTime expiresAt;

    @Schema(description = "Indicates whether the invitation has been accepted.")
    private boolean accepted;

    @Schema(description = "User ID who triggered the invitation.")
    private UUID invitedBy;
}
