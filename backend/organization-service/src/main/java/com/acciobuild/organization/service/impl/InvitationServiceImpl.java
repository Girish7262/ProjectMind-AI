package com.acciobuild.organization.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.organization.client.AuthServiceClient;
import com.acciobuild.organization.domain.event.InvitationAcceptedEvent;
import com.acciobuild.organization.domain.event.InvitationSentEvent;
import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.domain.model.OrganizationInvitation;
import com.acciobuild.organization.domain.repository.OrganizationInvitationRepository;
import com.acciobuild.organization.domain.repository.OrganizationMemberRepository;
import com.acciobuild.organization.domain.repository.OrganizationRepository;
import com.acciobuild.organization.dto.InvitationDto;
import com.acciobuild.organization.dto.InvitationRequest;
import com.acciobuild.organization.dto.UserResponse;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.exception.InvitationExpiredException;
import com.acciobuild.organization.exception.MemberAlreadyExistsException;
import com.acciobuild.organization.exception.OrganizationNotFoundException;
import com.acciobuild.organization.service.InvitationService;
import com.acciobuild.organization.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing user invitations life cycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {

    private final OrganizationInvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final AuthServiceClient authServiceClient;
    private final MembershipService membershipService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<InvitationDto> createInvitation(UUID organizationId, InvitationRequest request, UUID inviterUserId) {
        log.info("Creating invitation for email {} in organization {}", request.getEmail(), organizationId);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));

        // 1. Prevent inviting existing members
        UUID invitedUserId = null;
        try {
            ApiResponse<UserResponse> userResponse = authServiceClient.getUserByEmail(request.getEmail());
            if (userResponse != null && userResponse.getData() != null) {
                invitedUserId = userResponse.getData().getId();
                if (memberRepository.existsByOrganizationIdAndUserId(organizationId, invitedUserId)) {
                    throw new MemberAlreadyExistsException("This user is already a member of this organization.");
                }
            }
        } catch (MemberAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Invited email does not have a registered profile yet in Auth Service. Continuing invite.", e);
        }

        // 2. Prevent duplicate active invitations
        if (invitationRepository.existsByOrganizationIdAndEmailAndAcceptedFalse(organizationId, request.getEmail())) {
            List<OrganizationInvitation> activeInvites = invitationRepository.findActiveInvitations(organizationId, LocalDateTime.now());
            boolean hasDuplicate = activeInvites.stream().anyMatch(i -> i.getEmail().equalsIgnoreCase(request.getEmail()));
            if (hasDuplicate) {
                throw new BusinessException("An active invitation already exists for this email address.", "DUPLICATE_INVITATION");
            }
        }

        OrganizationInvitation invitation = new OrganizationInvitation();
        invitation.setId(UUID.randomUUID());
        invitation.setOrganization(org);
        invitation.setEmail(request.getEmail().toLowerCase().trim());
        invitation.setInviteToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // Expiration configurable (default 7 days)
        invitation.setAccepted(false);
        invitation.setInvitedBy(inviterUserId);

        OrganizationInvitation saved = invitationRepository.save(invitation);

        eventPublisher.publishEvent(new InvitationSentEvent(
                organizationId, 
                saved.getEmail(), 
                inviterUserId, 
                MdcHelper.getCorrelationId()
        ));

        return ApiResponse.<InvitationDto>builder()
                .status(201)
                .message("Invitation created and sent successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<InvitationDto> getInvitationByToken(String inviteToken) {
        OrganizationInvitation invitation = invitationRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new BusinessException("Invitation details not found for token.", "INVITATION_NOT_FOUND"));

        return ApiResponse.<InvitationDto>builder()
                .status(200)
                .message("Invitation fetched.")
                .data(mapToDto(invitation))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> acceptInvitation(String inviteToken, UUID userId) {
        log.info("Accepting invitation token by user ID: {}", userId);

        OrganizationInvitation invitation = invitationRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new BusinessException("Invitation not found.", "INVITATION_NOT_FOUND"));

        if (invitation.isAccepted()) {
            throw new BusinessException("Invitation already accepted.", "INVITATION_ALREADY_ACCEPTED");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvitationExpiredException("Invitation token has expired.");
        }

        // Add member to organization
        membershipService.addMember(invitation.getOrganization().getId(), userId, MemberRole.MEMBER);

        invitation.setAccepted(true);
        invitationRepository.save(invitation);

        eventPublisher.publishEvent(new InvitationAcceptedEvent(
                invitation.getOrganization().getId(), 
                invitation.getEmail(), 
                userId, 
                MdcHelper.getCorrelationId()
        ));

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Invitation accepted and member enrolled successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> revokeInvitation(UUID invitationId) {
        log.warn("Revoking invitation ID: {}", invitationId);

        OrganizationInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException("Invitation not found.", "INVITATION_NOT_FOUND"));

        invitationRepository.delete(invitation);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Invitation revoked successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<InvitationDto>> getInvitations(UUID organizationId) {
        List<OrganizationInvitation> list = invitationRepository.findByOrganizationId(organizationId);
        List<InvitationDto> content = list.stream().map(this::mapToDto).collect(Collectors.toList());

        return ApiResponse.<List<InvitationDto>>builder()
                .status(200)
                .message("Invitations list fetched.")
                .data(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<InvitationDto>> getInvitationsPaged(UUID organizationId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrganizationInvitation> pageResult = invitationRepository.findByOrganizationId(organizationId, pageable);

        List<InvitationDto> content = pageResult.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        PagedResponse<InvitationDto> paged = PagedResponse.<InvitationDto>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();

        return ApiResponse.<PagedResponse<InvitationDto>>builder()
                .status(200)
                .message("Paged invitations fetched.")
                .data(paged)
                .build();
    }

    private InvitationDto mapToDto(OrganizationInvitation i) {
        if (i == null) return null;
        return InvitationDto.builder()
                .id(i.getId())
                .organizationId(i.getOrganization().getId())
                .email(i.getEmail())
                .inviteToken(i.getInviteToken())
                .expiresAt(i.getExpiresAt())
                .accepted(i.isAccepted())
                .invitedBy(i.getInvitedBy())
                .build();
    }
}
