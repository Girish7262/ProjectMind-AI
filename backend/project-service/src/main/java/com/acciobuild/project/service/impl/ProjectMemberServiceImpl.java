package com.acciobuild.project.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.client.AuthServiceClient;
import com.acciobuild.project.domain.event.ProjectMemberAddedEvent;
import com.acciobuild.project.domain.event.ProjectMemberRemovedEvent;
import com.acciobuild.project.domain.event.ProjectOwnershipTransferredEvent;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectMember;
import com.acciobuild.project.domain.repository.ProjectMemberRepository;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.dto.ProjectMemberDto;
import com.acciobuild.project.dto.UserResponse;
import com.acciobuild.project.enums.ProjectMemberRole;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.exception.MemberAlreadyExistsException;
import com.acciobuild.project.exception.ProjectNotFoundException;
import com.acciobuild.project.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing project membership enrollment, role updates,
 * deactivations, and ownership transfers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final AuthServiceClient authServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectMemberDto> addMember(UUID projectId, UUID userId, String role) {
        log.info("Adding member user ID: {} with role: {} to project ID: {}", userId, role, projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new InvalidProjectOperationException("Cannot add members to a deleted project.");
        }

        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new MemberAlreadyExistsException("User is already a member of this project.");
        }

        // Validate user existence in Auth Service via OpenFeign client
        try {
            ApiResponse<UserResponse> userRes = authServiceClient.getUserById(userId);
            if (userRes == null || userRes.getData() == null) {
                throw new InvalidProjectOperationException("Collaborator profile not found in Auth Service.");
            }
        } catch (Exception e) {
            log.error("Feign validation failed for user ID: {}", userId, e);
            throw new InvalidProjectOperationException("Could not validate user profile from Auth Service: " + e.getMessage());
        }

        ProjectMember member = new ProjectMember();
        member.setId(UUID.randomUUID());
        member.setProject(project);
        member.setUserId(userId);
        member.setRole(ProjectMemberRole.valueOf(role.toUpperCase().trim()));
        member.setJoinedAt(LocalDateTime.now());
        member.setStatus("ACTIVE");

        ProjectMember saved = memberRepository.save(member);

        eventPublisher.publishEvent(new ProjectMemberAddedEvent(
                project.getOrganizationId(), projectId, userId, role, MdcHelper.getCorrelationId()));

        return ApiResponse.<ProjectMemberDto>builder()
                .status(201)
                .message("Member added to project successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectMemberDto> updateMemberRole(UUID projectId, UUID userId, String role) {
        log.info("Updating membership role for user ID: {} to {} in project: {}", userId, role, projectId);

        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new InvalidProjectOperationException("Project membership record not found."));

        ProjectMemberRole newRole = ProjectMemberRole.valueOf(role.toUpperCase().trim());

        // Safeguard sole maintainer demotion
        if (member.getRole() == ProjectMemberRole.MAINTAINER && newRole != ProjectMemberRole.MAINTAINER) {
            long maintainerCount = memberRepository.findByProjectId(projectId).stream()
                    .filter(m -> m.getRole() == ProjectMemberRole.MAINTAINER)
                    .count();
            if (maintainerCount <= 1) {
                throw new InvalidProjectOperationException("Cannot demote the sole project maintainer. Designate a new maintainer first.");
            }
        }

        // Ownership Transfer: If setting role to MAINTAINER, demote old maintainer to DEVELOPER
        if (newRole == ProjectMemberRole.MAINTAINER) {
            List<ProjectMember> members = memberRepository.findByProjectId(projectId);
            Optional<ProjectMember> currentOwner = members.stream()
                    .filter(m -> m.getRole() == ProjectMemberRole.MAINTAINER)
                    .findFirst();
            if (currentOwner.isPresent()) {
                ProjectMember oldOwner = currentOwner.get();
                if (!oldOwner.getUserId().equals(userId)) {
                    oldOwner.setRole(ProjectMemberRole.DEVELOPER);
                    memberRepository.save(oldOwner);
                    eventPublisher.publishEvent(new ProjectOwnershipTransferredEvent(
                            member.getProject().getOrganizationId(), projectId, oldOwner.getUserId(), userId, MdcHelper.getCorrelationId()));
                    log.info("Project ownership transferred: Demoted old owner {}", oldOwner.getUserId());
                }
            }
        }

        member.setRole(newRole);
        ProjectMember saved = memberRepository.save(member);

        return ApiResponse.<ProjectMemberDto>builder()
                .status(200)
                .message("Project member role updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> removeMember(UUID projectId, UUID userId) {
        log.warn("Removing member user ID: {} from project ID: {}", userId, projectId);

        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new InvalidProjectOperationException("Project membership record not found."));

        if (member.getRole() == ProjectMemberRole.MAINTAINER) {
            long maintainerCount = memberRepository.findByProjectId(projectId).stream()
                    .filter(m -> m.getRole() == ProjectMemberRole.MAINTAINER)
                    .count();
            if (maintainerCount <= 1) {
                throw new InvalidProjectOperationException("Cannot remove the sole project maintainer.");
            }
        }

        memberRepository.delete(member);

        eventPublisher.publishEvent(new ProjectMemberRemovedEvent(
                member.getProject().getOrganizationId(), projectId, userId, MdcHelper.getCorrelationId()));

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Project collaborator membership removed successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProjectMemberDto>> getMembers(UUID projectId) {
        List<ProjectMember> list = memberRepository.findByProjectId(projectId);
        List<ProjectMemberDto> content = list.stream().map(this::mapToDto).collect(Collectors.toList());

        return ApiResponse.<List<ProjectMemberDto>>builder()
                .status(200)
                .message("Project members list fetched.")
                .data(content)
                .build();
    }

    private ProjectMemberDto mapToDto(ProjectMember m) {
        if (m == null) return null;
        return ProjectMemberDto.builder()
                .id(m.getId())
                .projectId(m.getProject().getId())
                .userId(m.getUserId())
                .role(m.getRole().name())
                .joinedAt(m.getJoinedAt())
                .status(m.getStatus())
                .build();
    }
}
