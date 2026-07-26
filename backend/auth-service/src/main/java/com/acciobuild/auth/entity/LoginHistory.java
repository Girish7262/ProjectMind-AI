package com.acciobuild.auth.entity;

import com.acciobuild.common.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA Entity capturing login audit trails, location parameters, and credentials outcomes status.
 */
@Entity
@Table(
        name = "login_history",
        indexes = {
                @Index(columnList = "user_id", name = "idx_login_history_user"),
                @Index(columnList = "login_time", name = "idx_login_history_time")
        }
)
@Getter
@Setter
public class LoginHistory extends AuditEntity {

    @NotNull(message = "User is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_login_history_user"))
    private User user;

    @NotNull(message = "Login time is required.")
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Size(max = 45, message = "IP Address must not exceed 45 characters.")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 100, message = "Device must not exceed 100 characters.")
    @Column(name = "device", length = 100)
    private String device;

    @Size(max = 100, message = "Browser must not exceed 100 characters.")
    @Column(name = "browser", length = 100)
    private String browser;

    @Size(max = 100, message = "Operating System must not exceed 100 characters.")
    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Size(max = 100, message = "Country name must not exceed 100 characters.")
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 100, message = "City name must not exceed 100 characters.")
    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "success", nullable = false)
    private boolean success = true;

    @Size(max = 255, message = "Failure reason must not exceed 255 characters.")
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Override
    public String toString() {
        return "LoginHistory{" +
                "id=" + getId() +
                ", loginTime=" + loginTime +
                ", success=" + success +
                '}';
    }
}
