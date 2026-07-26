package com.acciobuild.auth.repository;

import com.acciobuild.auth.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against LoginHistories table.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    Page<LoginHistory> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT h FROM LoginHistory h WHERE h.user.id = :userId ORDER BY h.loginTime DESC")
    List<LoginHistory> findRecentLogins(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT h FROM LoginHistory h WHERE h.user.id = :userId AND h.success = false ORDER BY h.loginTime DESC")
    List<LoginHistory> findFailedLogins(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT h FROM LoginHistory h WHERE h.user.id = :userId AND h.success = true ORDER BY h.loginTime DESC")
    List<LoginHistory> findSuccessfulLogins(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT h FROM LoginHistory h WHERE h.user.id = :userId AND h.loginTime BETWEEN :start AND :end ORDER BY h.loginTime DESC")
    List<LoginHistory> findByDateRange(@Param("userId") UUID userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT COUNT(h) FROM LoginHistory h WHERE h.loginTime >= :startOfDay")
    long countTodayLogins(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(h) FROM LoginHistory h WHERE h.user.id = :userId AND h.success = false AND h.loginTime >= :sinceTime")
    long countFailedAttemptsSince(@Param("userId") UUID userId, @Param("sinceTime") LocalDateTime sinceTime);
}
