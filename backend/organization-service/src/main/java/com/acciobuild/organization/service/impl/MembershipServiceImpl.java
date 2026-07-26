package com.acciobuild.organization.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.organization.client.AuthServiceClient;
import com.acciobuild.organization.domain.event.MemberAddedEvent;
import com.acciobuild.organization.domain.event.MemberRemovedEvent;
import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.domain.model.OrganizationMember;
import com.acciobuild.organization.domain.repository.OrganizationMemberRepository;
import com.acciobuild.organization.domain.repository.OrganizationRepository;
import com.acciobuild.organization.dto.MemberDto;
import com.acciobuild.organization.dto.UserResponse;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import com.acciobuild.organization.exception.InvalidMembershipOperationException;
import com.acciobuild.organization.exception.MemberAlreadyExistsException;
import com.acciobuild.organization.exception.OrganizationNotFoundException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing organization memberships, owner rules, and transfers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipServiceImpl implements MembershipService {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthServiceClient authServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<MemberDto> addMember(UUID organizationId, UUID userId, MemberRole role) {
        log.info("Adding member user ID {} with role {} to organization {}", userId, role, organizationId);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));

        if (memberRepository.existsByOrganizationIdAndUserId(organizationId, userId)) {
            throw new MemberAlreadyExistsException("User is already a member of this organization.");
        }

        // Validate user existence and profile via AuthServiceClient Feign Client
        try {
            ApiResponse<UserResponse> userResponse = authServiceClient.getUserById(userId);
            if (userResponse == null || userResponse.getData() == null) {
                throw new InvalidMembershipOperationException("Invited user profile details not found in Auth Service.");
            }
        } catch (Exception e) {
            log.error("Failed to validate user existence in Auth Service via OpenFeign client", e);
            throw new InvalidMembershipOperationException("Could not validate user profile from Auth Service: " + e.getMessage());
        }

        OrganizationMember member = new OrganizationMember();
        member.setId(UUID.randomUUID());
        member.setOrganization(org);
        member.setUserId(userId);
        member.setRole(role);
        member.setStatus(MemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());

        OrganizationMember saved = memberRepository.save(member);

        eventPublisher.publishEvent(new MemberAddedEvent(
                organizationId, 
                userId, 
                role.name(), 
                MdcHelper.getCorrelationId()
        ));

        return ApiResponse.<MemberDto>builder()
                .status(201)
                .message("Member added successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<MemberDto> updateMemberRole(UUID organizationId, UUID userId, MemberRole role) {
        log.info("Updating membership role for user ID {} to {} in organization {}", userId, role, organizationId);

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new InvalidMembershipOperationException("Membership record not found."));

        // Safeguard sole Owner removal
        if (member.getRole() == MemberRole.OWNER && role != MemberRole.OWNER) {
            long ownerCount = memberRepository.findByOrganizationId(organizationId).stream()
                    .filter(m -> m.getRole() == MemberRole.OWNER)
                    .count();
            if (ownerCount <= 1) {
                throw new InvalidMembershipOperationException("Cannot demote the sole organization owner. Transfer ownership first.");
            }
        }

        // Ownership Transfer: If setting role to OWNER, demote the current owner to ADMIN
        if (role == MemberRole.OWNER) {
            List<OrganizationMember> members = memberRepository.findByOrganizationId(organizationId);
            Optional<OrganizationMember> currentOwner = members.stream()
                    .filter(m -> m.getRole() == MemberRole.OWNER)
                    .findFirst();
            if (currentOwner.isPresent()) {
                OrganizationMember oldOwner = currentOwner.get();
                if (!oldOwner.getUserId().equals(userId)) {
                    oldOwner.setRole(MemberRole.ADMIN);
                    memberRepository.save(oldOwner);
                    log.info("Ownership transfer complete: Demoted old owner {}", oldOwner.getUserId());
                }
            }
        }

        member.setRole(role);
        OrganizationMember saved = memberRepository.save(member);

        return ApiResponse.<MemberDto>builder()
                .status(200)
                .message("Membership role updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<MemberDto> updateMemberStatus(UUID organizationId, UUID userId, MemberStatus status) {
        log.info("Updating status for user ID {} in organization {} to {}", userId, organizationId, status);

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new InvalidMembershipOperationException("Membership record not found."));

        if (member.getRole() == MemberRole.OWNER && status != MemberStatus.ACTIVE) {
            throw new InvalidMembershipOperationException("Cannot change the status of the organization owner.");
        }

        member.setStatus(status);
        OrganizationMember saved = memberRepository.save(member);

        return ApiResponse.<MemberDto>builder()
                .status(200)
                .message("Membership status updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> removeMember(UUID organizationId, UUID userId) {
        log.warn("Removing member user ID {} from organization {}", userId, organizationId);

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new InvalidMembershipOperationException("Membership record not found."));

        if (member.getRole() == MemberRole.OWNER) {
            throw new InvalidMembershipOperationException("Organization owner cannot be removed.");
        }

        memberRepository.delete(member);

        eventPublisher.publishEvent(new MemberRemovedEvent(organizationId, userId, MdcHelper.getCorrelationId()));

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Membership association removed successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MemberDto>> getMembers(UUID organizationId) {
        List<OrganizationMember> members = memberRepository.findByOrganizationId(organizationId);
        List<MemberDto> content = members.stream().map(this::mapToDto).collect(Collectors.toList());

        return ApiResponse.<List<MemberDto>>builder()
                .status(200)
                .message("Organization members fetched.")
                .data(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<MemberDto>> getMembersPaged(UUID organizationId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrganizationMember> pageResult = memberRepository.findByOrganizationId(organizationId, pageable);

        List<MemberDto> content = pageResult.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        PagedResponse<MemberDto> paged = PagedResponse.<MemberDto>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();

        return ApiResponse.<PagedResponse<MemberDto>>builder()
                .status(200)
                .message("Paged members fetched.")
                .data(paged)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MemberDto>> getUserMemberships(UUID userId) {
        List<OrganizationMember> memberships = memberRepository.findByUserId(userId);
        List<MemberDto> content = memberships.stream().map(this::mapToDto).collect(Collectors.toList());

        return ApiResponse.<List<MemberDto>>builder()
                .status(200)
                .message("User memberships fetched.")
                .data(content)
                .build();
    }

    private MemberDto mapToDto(OrganizationMember m) {
        if (m == null) return null;
        return MemberDto.builder()
                .id(m.getId())
                .organizationId(m.getOrganization().getId())
                .userId(m.getUserId())
                .role(m.getRole().name())
                .joinedAt(m.getJoinedAt())
                .status(m.getStatus().name())
                .build();
    }
}
