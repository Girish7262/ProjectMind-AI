package com.acciobuild.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

/**
 * Request payload payload for registering a new user profile.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email formatting format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    @NotBlank(message = "First name is required.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotNull(message = "Organization ID is required.")
    private UUID organizationId;
}
