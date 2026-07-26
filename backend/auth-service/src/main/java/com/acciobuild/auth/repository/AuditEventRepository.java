package com.acciobuild.auth.repository;

import com.acciobuild.auth.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against AuditEvents table.
 * Supports specification executers for dynamic search filters, pagination, and sorting.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {

    /**
     * Retrieve audits events mapping specific user ID.
     */
    Page<AuditEvent> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Retrieve audits events recorded within a specific date time range bounds.
     */
    @Query("SELECT ae FROM AuditEvent ae WHERE ae.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditEvent> findByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
}
