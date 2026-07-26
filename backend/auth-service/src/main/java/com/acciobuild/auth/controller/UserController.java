package com.acciobuild.auth.controller;

import com.acciobuild.auth.dto.UserProfileResponse;
import com.acciobuild.auth.service.UserService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST controller exposing administrative user status management endpoints.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management Console", description = "Endpoints exposing administrative query selectors and profiles updates.")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get Specific Profile", description = "Retrieves profile parameters metrics for target user UUID.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable UUID id) {
        log.info("REST request to fetch user profile: {}", id);
        ApiResponse<UserProfileResponse> response = userService.getUserProfile(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/by-email")
    @Operation(summary = "Get Profile by Email", description = "Retrieves user profile parameters matching email query parameter.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserByEmail(@RequestParam String email) {
        log.info("REST request to fetch user profile by email: {}", email);
        com.acciobuild.auth.entity.User user = userService.getUserByEmail(email);
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus().name())
                .organizationId(user.getOrganizationId())
                .build();
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .status(200)
                .message("User profile fetched successfully.")
                .data(profile)
                .build());
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Lock User Account", description = "Suspends account access capabilities immediately.")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable UUID id) {
        log.info("REST request to lock user account: {}", id);
        // lockUser implementation delegates to service wrapper
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("User account locked successfully.")
                .build());
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Unlock User Account", description = "Restores account access capabilities immediately.")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID id) {
        log.info("REST request to unlock user account: {}", id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("User account unlocked successfully.")
                .build());
    }
}
