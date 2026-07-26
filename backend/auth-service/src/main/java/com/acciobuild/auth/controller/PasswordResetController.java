package com.acciobuild.auth.controller;

import com.acciobuild.auth.dto.ForgotPasswordRequest;
import com.acciobuild.auth.dto.ResetPasswordRequest;
import com.acciobuild.auth.service.PasswordResetService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing password recovery request triggers and token reset completion endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password Recovery Console", description = "Endpoints managing password forgot requests and secure token reset confirmations.")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * POST endpoint triggering the Forgot Password request.
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request Forgot Password Link",
            description = "Triggers a secure UUID reset token, saves it, and delivers an email notification containing recovery steps to the registered user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset email sent successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"timestamp\":\"2026-07-23T23:45:00\",\"status\":200,\"message\":\"Password reset token generated and sent to email successfully.\",\"data\":null,\"metadata\":{}}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Registered email not found.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Not Found Response",
                                    value = "{\"timestamp\":\"2026-07-23T23:45:00\",\"status\":404,\"message\":\"User not found with email: user@acciobuild.com\",\"data\":null,\"metadata\":{}}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many requests (rate limit exceeded).",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Rate Limit Exceeded",
                                    value = "{\"timestamp\":\"2026-07-23T23:45:00\",\"status\":400,\"message\":\"Too many attempts. Rate limit exceeded. Please try again after 15 minutes.\",\"data\":null,\"metadata\":{}}"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest) {
        
        log.info("REST request to generate forgot password token for email: {}", request.getEmail());
        
        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        String device = getDeviceFromUserAgent(userAgent);
        String browser = getBrowserFromUserAgent(userAgent);
        
        ApiResponse<Void> response = passwordResetService.forgotPassword(request, ipAddress, device, browser);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * POST endpoint executing the Password Reset confirmation.
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Confirm Password Reset",
            description = "Consumes a secure reset token, validates the password complexity policy, updates user credentials, and revokes all active Redis and JWT sessions."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password updated successfully. Active sessions revoked.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"timestamp\":\"2026-07-23T23:45:00\",\"status\":200,\"message\":\"Password updated successfully. Active sessions revoked.\",\"data\":null,\"metadata\":{}}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid token, expired token, password mismatch, complexity failure, or history reuse violation.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error / Complexity Violation",
                                    value = "{\"timestamp\":\"2026-07-23T23:45:00\",\"status\":400,\"message\":\"Password does not meet complexity requirements: Minimum 12 characters, at least one uppercase, lowercase, digit, and special character.\",\"data\":null,\"metadata\":{}}"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest servletRequest) {
        
        log.info("REST request to confirm password reset using token");
        
        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        String device = getDeviceFromUserAgent(userAgent);
        String browser = getBrowserFromUserAgent(userAgent);
        
        ApiResponse<Void> response = passwordResetService.resetPassword(request, ipAddress, device, browser);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    /**
     * Extracts simple device type classification from User-Agent string.
     */
    private String getDeviceFromUserAgent(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        String ua = userAgent.toLowerCase();
        if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad")) {
            return "Mobile Device";
        }
        return "Desktop PC";
    }

    /**
     * Extracts simple browser name classifications from User-Agent string.
     */
    private String getBrowserFromUserAgent(String userAgent) {
        if (userAgent == null) return "Unknown Browser";
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome")) return "Google Chrome";
        if (ua.contains("firefox")) return "Mozilla Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Apple Safari";
        if (ua.contains("edge")) return "Microsoft Edge";
        return "Web Browser";
    }
}
