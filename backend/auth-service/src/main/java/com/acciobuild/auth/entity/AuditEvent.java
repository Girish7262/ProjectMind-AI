package com.acciobuild.auth.entity;

import com.acciobuild.auth.enums.AuditEventStatus;
import com.acciobuild.auth.enums.AuditEventType;
import com.acciobuild.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping trace audits and security event entries.
 */
@Entity
@Table(
        name = "audit_events",
        indexes = {
                @Index(columnList = "user_id", name = "idx_audit_events_user_id"),
                @Index(columnList = "username", name = "idx_audit_events_username"),
                @Index(columnList = "organization_id", name = "idx_audit_events_org_id"),
                @Index(columnList = "event_type", name = "idx_audit_events_event_type"),
                @Index(columnList = "timestamp", name = "idx_audit_events_timestamp")
        }
)
@Getter
@Setter
public class AuditEvent extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Size(max = 150, message = "Username must not exceed 150 characters.")
    @Column(name = "username", length = 150)
    private String username;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Size(max = 100, message = "Session ID must not exceed 100 characters.")
    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Size(max = 45, message = "IP Address must not exceed 45 characters.")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 100, message = "Browser must not exceed 100 characters.")
    @Column(name = "browser", length = 100)
    private String browser;

    @Size(max = 100, message = "Device must not exceed 100 characters.")
    @Column(name = "device", length = 100)
    private String device;

    @Size(max = 100, message = "Operating System must not exceed 100 characters.")
    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Size(max = 255, message = "Endpoint URI must not exceed 255 characters.")
    @Column(name = "endpoint", length = 255)
    private String endpoint;

    @Size(max = 10, message = "HTTP Method must not exceed 10 characters.")
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @NotNull(message = "Timestamp is required.")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @NotNull(message = "Event type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    @NotNull(message = "Event status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 20)
    private AuditEventStatus eventStatus;

    @Size(max = 500, message = "Failure reason must not exceed 500 characters.")
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Size(max = 100, message = "Correlation ID must not exceed 100 characters.")
    @Column(name = "correlation_id", length = 100)
    private String correlationId;
}
