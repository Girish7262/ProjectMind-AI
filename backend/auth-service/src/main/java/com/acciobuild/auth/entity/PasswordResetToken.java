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
 * JPA Entity mapping one-time secure password reset tokens.
 */
@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"token"}, name = "uk_password_reset_token")
        },
        indexes = {
                @Index(columnList = "token", name = "idx_password_reset_token"),
                @Index(columnList = "user_id", name = "idx_password_reset_token_user_id")
        }
)
@Getter
@Setter
public class PasswordResetToken extends AuditEntity {

    @NotBlank(message = "Token string is required.")
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull(message = "User reference is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_password_reset_token_user"))
    private User user;

    @NotNull(message = "Expiration timestamp is required.")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Size(max = 45, message = "IP Address must not exceed 45 characters.")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 100, message = "Device must not exceed 100 characters.")
    @Column(name = "device", length = 100)
    private String device;

    @Size(max = 100, message = "Browser must not exceed 100 characters.")
    @Column(name = "browser", length = 100)
    private String browser;

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + getId() +
                ", token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}
