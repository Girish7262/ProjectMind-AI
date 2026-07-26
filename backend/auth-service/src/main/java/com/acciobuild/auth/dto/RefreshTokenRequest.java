package com.acciobuild.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for rotating access tokens.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;
}
