package com.acciobuild.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload mapping security audit events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload mapping security audit event details.")
public class AuditEventResponse {

    @Schema(description = "Unique Identifier of the audit event.")
    private UUID id;

    @Schema(description = "User ID who performed the action.")
    private UUID userId;

    @Schema(description = "Email address / username of the user.")
    private String username;

    @Schema(description = "User organization ID constraint.")
    private UUID organizationId;

    @Schema(description = "Client web session ID.")
    private String sessionId;

    @Schema(description = "IP address of request origin.")
    private String ipAddress;

    @Schema(description = "Client browser name.")
    private String browser;

    @Schema(description = "Client device name.")
    private String device;

    @Schema(description = "Client operating system name.")
    private String operatingSystem;

    @Schema(description = "Target API endpoint URI.")
    private String endpoint;

    @Schema(description = "HTTP Method (e.g. GET, POST).")
    private String httpMethod;

    @Schema(description = "Timestamp when the event occurred.")
    private LocalDateTime timestamp;

    @Schema(description = "Event Type (e.g. LOGIN_SUCCESS, PASSWORD_CHANGED).")
    private String eventType;

    @Schema(description = "Event Outcome Status (e.g. SUCCESS, FAILED, WARNING).")
    private String eventStatus;

    @Schema(description = "Failure message if exception was thrown.")
    private String failureReason;

    @Schema(description = "Correlation ID for request tracing.")
    private String correlationId;
}
