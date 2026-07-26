package com.acciobuild.auth.dto;

import com.acciobuild.common.validation.UuidConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload containing token validator and new credentials to perform password update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to reset a user password using a token.")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required.")
    @UuidConstraint(message = "Reset token must be a valid UUID.")
    @Schema(description = "Secure UUID reset token.", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private String token;

    @NotBlank(message = "New password is required.")
    @Schema(description = "New password conforming to security rules.", example = "NewP@ssw0rd2026", required = true)
    private String newPassword;

    @NotBlank(message = "Confirmation password is required.")
    @Schema(description = "Confirm new password.", example = "NewP@ssw0rd2026", required = true)
    private String confirmPassword;
}
