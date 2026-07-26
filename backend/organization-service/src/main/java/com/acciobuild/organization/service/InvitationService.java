package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.organization.dto.InvitationDto;
import com.acciobuild.organization.dto.InvitationRequest;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing tenant invitations actions.
 */
public interface InvitationService {

    /**
     * Creates and registers a new organization invitation and generates a secure token.
     */
    ApiResponse<InvitationDto> createInvitation(UUID organizationId, InvitationRequest request, UUID inviterUserId);

    /**
     * Resolves invitation details by its secure token.
     */
    ApiResponse<InvitationDto> getInvitationByToken(String inviteToken);

    /**
     * Consumes and accepts an invitation, enrolling the invitee email into organization membership.
     */
    ApiResponse<Void> acceptInvitation(String inviteToken, UUID userId);

    /**
     * Cancels / revokes a pending invitation.
     */
    ApiResponse<Void> revokeInvitation(UUID invitationId);

    /**
     * Lists all invitations sent by an organization.
     */
    ApiResponse<List<InvitationDto>> getInvitations(UUID organizationId);

    /**
     * Paginates organization invitations.
     */
    ApiResponse<PagedResponse<InvitationDto>> getInvitationsPaged(UUID organizationId, int page, int size, String sortBy, String direction);
}
