package com.acciobuild.project.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.client.OrganizationServiceClient;
import com.acciobuild.project.domain.event.ProjectCreatedEvent;
import com.acciobuild.project.domain.event.ProjectDeletedEvent;
import com.acciobuild.project.domain.event.ProjectUpdatedEvent;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectMember;
import com.acciobuild.project.domain.model.ProjectSettings;
import com.acciobuild.project.domain.repository.ProjectMemberRepository;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.dto.OrganizationDto;
import com.acciobuild.project.dto.ProjectDto;
import com.acciobuild.project.dto.ProjectRequest;
import com.acciobuild.project.dto.SettingsDto;
import com.acciobuild.project.enums.ProjectMemberRole;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.enums.ProjectVisibility;
import com.acciobuild.project.exception.*;
import com.acciobuild.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation managing Project aggregate creation, validations,
 * soft deletions, and cross-service limits integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final OrganizationServiceClient organizationServiceClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectDto> createProject(ProjectRequest request, UUID organizationId, UUID creatorUserId) {
        log.info("Attempting to provision project: {} inside organization: {}", request.getProjectName(), organizationId);

        // 1. Validate Organization existence and status
        try {
            ApiResponse<OrganizationDto> orgRes = organizationServiceClient.getOrganizationById(organizationId);
            if (orgRes == null || orgRes.getData() == null || !"ACTIVE".equals(orgRes.getData().getStatus())) {
                throw new InvalidProjectOperationException("Organization is not active or suspended.");
            }
        } catch (Exception e) {
            log.error("Organization validation failed via Feign", e);
            throw new InvalidProjectOperationException("Could not validate organization tenant profile: " + e.getMessage());
        }

        // 2. Validate Project Limits limits configurations
        try {
            ApiResponse<SettingsDto> settingsRes = organizationServiceClient.getSettings(organizationId);
            if (settingsRes != null && settingsRes.getData() != null) {
                long currentCount = projectRepository.countByOrganizationId(organizationId);
                if (currentCount >= settingsRes.getData().getMaxProjects()) {
                    throw new ProjectLimitExceededException("Organization has exceeded its maximum projects creation limit.");
                }
            }
        } catch (ProjectLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to validate organization settings limits. Proceeding with default values.", e);
        }

        // 3. Validate code and name uniqueness collisions
        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateProjectException("Project code identifier is already in use: " + request.getProjectCode());
        }
        if (projectRepository.existsByOrganizationIdAndProjectName(organizationId, request.getProjectName())) {
            throw new DuplicateProjectException("Project name is already in use inside this organization.");
        }

        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setOrganizationId(organizationId);
        project.setProjectCode(request.getProjectCode());
        project.setProjectName(request.getProjectName());
        project.setDisplayName(request.getDisplayName());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.PLANNING);
        project.setVisibility(ProjectVisibility.valueOf(request.getVisibility().toUpperCase().trim()));
        project.setCreatedBy(creatorUserId);
        project.setUpdatedBy(creatorUserId);

        // Map default ProjectSettings
        ProjectSettings settings = new ProjectSettings();
        settings.setProject(project);
        settings.setAiEnabled(true);
        settings.setKnowledgeCaptureEnabled(true);
        settings.setCodeAnalysisEnabled(true);
        settings.setDocumentationEnabled(true);
        settings.setMaxRepositories(5);
        settings.setDefaultBranch("main");
        project.setSettings(settings);

        Project saved = projectRepository.save(project);

        // Auto enroll creator as MAINTAINER
        ProjectMember member = new ProjectMember();
        member.setId(UUID.randomUUID());
        member.setProject(saved);
        member.setUserId(creatorUserId);
        member.setRole(ProjectMemberRole.MAINTAINER);
        member.setJoinedAt(LocalDateTime.now());
        member.setStatus("ACTIVE");
        memberRepository.save(member);

        eventPublisher.publishEvent(new ProjectCreatedEvent(organizationId, projectId, saved.getProjectCode(), MdcHelper.getCorrelationId()));

        return ApiResponse.<ProjectDto>builder()
                .status(201)
                .message("Project created successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectDto> updateProject(UUID projectId, ProjectRequest request) {
        log.info("Updating project ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new InvalidProjectOperationException("Deleted projects cannot be modified.");
        }
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedException("Archived projects are read-only.");
        }

        // Check code collisions if modified
        if (!project.getProjectCode().equals(request.getProjectCode()) &&
                projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateProjectException("Project code already in use.");
        }

        // Check name collisions in organization if modified
        if (!project.getProjectName().equals(request.getProjectName()) &&
                projectRepository.existsByOrganizationIdAndProjectName(project.getOrganizationId(), request.getProjectName())) {
            throw new DuplicateProjectException("Project name already in use inside organization.");
        }

        project.setProjectCode(request.getProjectCode());
        project.setProjectName(request.getProjectName());
        project.setDisplayName(request.getDisplayName());
        project.setDescription(request.getDescription());
        project.setVisibility(ProjectVisibility.valueOf(request.getVisibility().toUpperCase().trim()));

        Project saved = projectRepository.save(project);

        eventPublisher.publishEvent(new ProjectUpdatedEvent(project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));

        return ApiResponse.<ProjectDto>builder()
                .status(200)
                .message("Project updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> deleteProject(UUID projectId) {
        log.warn("Soft deleting project ID: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        project.setStatus(ProjectStatus.DELETED);
        projectRepository.save(project);

        eventPublisher.publishEvent(new ProjectDeletedEvent(project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Project soft deleted successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ProjectDto> getProjectById(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        return ApiResponse.<ProjectDto>builder()
                .status(200)
                .message("Project fetched.")
                .data(mapToDto(project))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ProjectDto> getProjectByCode(String projectCode) {
        Project project = projectRepository.findByProjectCode(projectCode)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found for code: " + projectCode));
        return ApiResponse.<ProjectDto>builder()
                .status(200)
                .message("Project fetched.")
                .data(mapToDto(project))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectDto> updateProjectStatus(UUID projectId, String status) {
        log.info("Updating status of project ID: {} to {}", projectId, status);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        ProjectStatus current = project.getStatus();
        ProjectStatus target = ProjectStatus.valueOf(status.toUpperCase().trim());

        boolean valid = false;
        if (current == ProjectStatus.PLANNING && target == ProjectStatus.ACTIVE) valid = true;
        else if (current == ProjectStatus.ACTIVE && target == ProjectStatus.ON_HOLD) valid = true;
        else if (current == ProjectStatus.ON_HOLD && target == ProjectStatus.ACTIVE) valid = true;
        else if (current == ProjectStatus.ACTIVE && target == ProjectStatus.ARCHIVED) valid = true;
        else if (current == ProjectStatus.ARCHIVED && target == ProjectStatus.RESTORED) valid = true;
        else if (current == ProjectStatus.RESTORED && target == ProjectStatus.ACTIVE) valid = true;
        else if (current == ProjectStatus.ACTIVE && target == ProjectStatus.DELETED) valid = true;
        else if (current == ProjectStatus.ARCHIVED && target == ProjectStatus.DELETED) valid = true;
        else if (current == ProjectStatus.ACTIVE && target == ProjectStatus.SUSPENDED) valid = true;
        else if (current == ProjectStatus.SUSPENDED && target == ProjectStatus.ACTIVE) valid = true;

        if (!valid) {
            throw new com.acciobuild.project.exception.InvalidProjectStateException(
                    "State transition from " + current + " to " + target + " is not permitted.");
        }

        project.setStatus(target);
        Project saved = projectRepository.save(project);

        // Publish transition-specific events
        if (target == ProjectStatus.ACTIVE) {
            eventPublisher.publishEvent(new com.acciobuild.project.domain.event.ProjectActivatedEvent(
                    project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));
        } else if (target == ProjectStatus.SUSPENDED || target == ProjectStatus.ON_HOLD) {
            eventPublisher.publishEvent(new com.acciobuild.project.domain.event.ProjectSuspendedEvent(
                    project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));
        } else if (target == ProjectStatus.ARCHIVED) {
            eventPublisher.publishEvent(new com.acciobuild.project.domain.event.ProjectArchivedEvent(
                    project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));
        } else if (target == ProjectStatus.RESTORED) {
            eventPublisher.publishEvent(new com.acciobuild.project.domain.event.ProjectRestoredEvent(
                    project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));
        } else if (target == ProjectStatus.DELETED) {
            eventPublisher.publishEvent(new com.acciobuild.project.domain.event.ProjectSoftDeletedEvent(
                    project.getOrganizationId(), projectId, MdcHelper.getCorrelationId()));
        }

        return ApiResponse.<ProjectDto>builder()
                .status(200)
                .message("Project status updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    private ProjectDto mapToDto(Project p) {
        if (p == null) return null;
        return ProjectDto.builder()
                .id(p.getId())
                .organizationId(p.getOrganizationId())
                .projectCode(p.getProjectCode())
                .projectName(p.getProjectName())
                .displayName(p.getDisplayName())
                .description(p.getDescription())
                .status(p.getStatus().name())
                .visibility(p.getVisibility().name())
                .createdBy(p.getCreatedBy())
                .updatedBy(p.getUpdatedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
