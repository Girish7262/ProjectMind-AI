package com.acciobuild.auth.repository;

import com.acciobuild.auth.entity.PasswordResetToken;
import com.acciobuild.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against PasswordResetTokens table.
 */
@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Retrieve active or inactive password reset token by the token string.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Retrieve the latest unused and unexpired reset token for a specific user.
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user AND t.used = false AND t.expiresAt > :now AND t.deleted = false")
    Optional<PasswordResetToken> findActiveTokenByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Invalidate all existing unused tokens for a user (prevents multiple active tokens/replay attacks).
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.deleted = true WHERE t.user.id = :userId AND t.used = false")
    void invalidateExistingTokens(@Param("userId") UUID userId);
}
