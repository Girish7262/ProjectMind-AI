package com.acciobuild.organization.domain.repository;

import com.acciobuild.organization.domain.model.OrganizationInvitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against OrganizationInvitations table.
 * Supports bulk cleanup and specifications operations.
 */
@Repository
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, UUID>, JpaSpecificationExecutor<OrganizationInvitation> {

    /**
     * Retrieve invite matching secure token string.
     */
    Optional<OrganizationInvitation> findByInviteToken(String inviteToken);

    /**
     * Retrieve active or inactive invitations mapped to a specific organization.
     */
    List<OrganizationInvitation> findByOrganizationId(UUID organizationId);

    /**
     * Paginates invitations mapped to a specific organization.
     */
    Page<OrganizationInvitation> findByOrganizationId(UUID organizationId, Pageable pageable);

    /**
     * Retrieve pending/accepted invitations matching target email.
     */
    List<OrganizationInvitation> findByEmail(String email);

    /**
     * Check if a pending invitation exists for an email within an organization.
     */
    boolean existsByOrganizationIdAndEmailAndAcceptedFalse(UUID organizationId, String email);

    /**
     * Find active pending (unaccepted) invitations that are not yet expired.
     */
    @Query("SELECT oi FROM OrganizationInvitation oi WHERE oi.organization.id = :orgId AND oi.accepted = false AND oi.expiresAt > :now")
    List<OrganizationInvitation> findActiveInvitations(@Param("orgId") UUID orgId, @Param("now") LocalDateTime now);

    /**
     * Find unaccepted expired invitations.
     */
    List<OrganizationInvitation> findByExpiresAtBeforeAndAcceptedFalse(LocalDateTime now);

    /**
     * Delete unaccepted expired invitations.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OrganizationInvitation oi WHERE oi.expiresAt < :now AND oi.accepted = false")
    int deleteExpiredInvitations(@Param("now") LocalDateTime now);
}
