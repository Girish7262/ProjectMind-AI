package com.acciobuild.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for session logouts.
 */
@Data
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required.")
    private String refreshToken;
}
