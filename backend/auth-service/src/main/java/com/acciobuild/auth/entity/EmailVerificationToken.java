package com.acciobuild.auth.entity;

import com.acciobuild.common.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping one-time registration verification tokens.
 */
@Entity
@Table(
        name = "email_verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"token"}, name = "uk_email_verification_token")
        },
        indexes = {
                @Index(columnList = "token", name = "idx_email_verification_token")
        }
)
@Getter
@Setter
public class EmailVerificationToken extends AuditEntity {

    @NotBlank(message = "Token string is required.")
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull(message = "User reference is required.")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_email_verification_token_user"))
    private User user;

    @NotNull(message = "Expiration timestamp is required.")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "resend_attempts", nullable = false)
    private int resendAttempts = 0;

    @Override
    public String toString() {
        return "EmailVerificationToken{" +
                "id=" + getId() +
                ", expiresAt=" + expiresAt +
                ", resendAttempts=" + resendAttempts +
                '}';
    }
}
