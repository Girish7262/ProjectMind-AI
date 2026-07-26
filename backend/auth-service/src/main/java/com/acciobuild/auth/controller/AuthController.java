package com.acciobuild.auth.controller;

import com.acciobuild.auth.dto.*;
import com.acciobuild.auth.service.AuthService;
import com.acciobuild.auth.service.UserService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST controller exposing user registration, login, logout, and token rotation endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Console", description = "Endpoints managing registrations, logins, rotations, and credentials changes.")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register User Profile", description = "Creates a new user profile aligned to tenant organization ID constraint parameters.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register email: {}", request.getEmail());
        ApiResponse<UserProfileResponse> response = authService.register(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate User credentials", description = "Validates credentials, logging client metadata records and returning JWT access tokens.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        log.info("REST request to login email: {}", request.getEmail());
        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        ApiResponse<LoginResponse> response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate Access Token", description = "Rotates session refresh token, returning renewed access JWT token.")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("REST request to rotate refresh token");
        ApiResponse<LoginResponse> response = authService.refresh(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Terminate Session", description = "Revokes refresh token status and clears credentials cache contexts.")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        log.info("REST request to logout");
        ApiResponse<Void> response = authService.logout(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update Password Credentials", description = "Modifies active user password credentials, validating strength complexity policies.")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = SecurityContextHelper.getCurrentUserId();
        log.info("REST request to change password for user: {}", userId);
        ApiResponse<Void> response = authService.changePassword(userId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Profile Info", description = "Returns active user parameters profiles and roles details lists.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        UUID userId = SecurityContextHelper.getCurrentUserId();
        log.info("REST request to fetch profile for user: {}", userId);
        ApiResponse<UserProfileResponse> response = userService.getUserProfile(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
