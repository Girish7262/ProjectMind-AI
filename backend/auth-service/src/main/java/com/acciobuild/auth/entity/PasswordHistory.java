package com.acciobuild.auth.entity;

import com.acciobuild.common.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA Entity capturing password history entries to prevent password reuse.
 */
@Entity
@Table(
        name = "password_histories",
        indexes = {
                @Index(columnList = "user_id", name = "idx_password_histories_user_id"),
                @Index(columnList = "created_at", name = "idx_password_histories_created_at")
        }
)
@Getter
@Setter
public class PasswordHistory extends AuditEntity {

    @NotNull(message = "User is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_password_histories_user"))
    private User user;

    @NotBlank(message = "Password hash is required.")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
