package com.acciobuild.auth.entity;

import com.acciobuild.common.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity storing refresh tokens associated with active user sessions.
 * Maps client details and expiration parameters.
 */
@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"token"}, name = "uk_refresh_tokens_token")
        },
        indexes = {
                @Index(columnList = "token", name = "idx_refresh_tokens_token")
        }
)
@Getter
@Setter
public class RefreshToken extends AuditEntity {

    @NotBlank(message = "Token is required.")
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull(message = "Expiration time is required.")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Size(max = 100, message = "Device signature must not exceed 100 characters.")
    @Column(name = "device", length = 100)
    private String device;

    @Size(max = 45, message = "IP Address must not exceed 45 characters.")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @NotNull(message = "User is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + getId() +
                ", revoked=" + revoked +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
