package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.organization.dto.MemberDto;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing tenant organization memberships operations.
 */
public interface MembershipService {

    /**
     * Enrolls a user with a specific role into an organization.
     */
    ApiResponse<MemberDto> addMember(UUID organizationId, UUID userId, MemberRole role);

    /**
     * Updates an existing member's role (e.g. OWNER, ADMIN, MEMBER).
     */
    ApiResponse<MemberDto> updateMemberRole(UUID organizationId, UUID userId, MemberRole role);

    /**
     * Suspends or blocks a member inside an organization.
     */
    ApiResponse<MemberDto> updateMemberStatus(UUID organizationId, UUID userId, MemberStatus status);

    /**
     * Revokes organization access for a user.
     */
    ApiResponse<Void> removeMember(UUID organizationId, UUID userId);

    /**
     * Lists all members enrolled in an organization.
     */
    ApiResponse<List<MemberDto>> getMembers(UUID organizationId);

    /**
     * Paginates organization members.
     */
    ApiResponse<PagedResponse<MemberDto>> getMembersPaged(UUID organizationId, int page, int size, String sortBy, String direction);

    /**
     * Resolves user organization memberships.
     */
    ApiResponse<List<MemberDto>> getUserMemberships(UUID userId);
}
