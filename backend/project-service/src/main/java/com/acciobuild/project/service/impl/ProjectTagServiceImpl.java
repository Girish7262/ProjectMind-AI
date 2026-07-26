package com.acciobuild.project.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.domain.event.ProjectTagCreatedEvent;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectTag;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.domain.repository.ProjectTagRepository;
import com.acciobuild.project.dto.ProjectTagDto;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.exception.ProjectArchivedException;
import com.acciobuild.project.exception.ProjectNotFoundException;
import com.acciobuild.project.service.ProjectTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing project tags mapping operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectTagServiceImpl implements ProjectTagService {

    private final ProjectTagRepository tagRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectTagDto> addTag(UUID projectId, ProjectTagDto dto) {
        log.info("Adding tag {} to project ID: {}", dto.getTagName(), projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new InvalidProjectOperationException("Cannot add tags to a deleted project.");
        }
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedException("Cannot add tags to an archived project.");
        }

        // Validate duplicates
        boolean exists = tagRepository.findByProjectId(projectId).stream()
                .anyMatch(t -> t.getTagName().equalsIgnoreCase(dto.getTagName()));
        if (exists) {
            throw new InvalidProjectOperationException("Tag name already exists for this project.");
        }

        ProjectTag tag = new ProjectTag();
        tag.setId(UUID.randomUUID());
        tag.setProject(project);
        tag.setTagName(dto.getTagName().toLowerCase().trim());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#6366f1");

        ProjectTag saved = tagRepository.save(tag);

        eventPublisher.publishEvent(new ProjectTagCreatedEvent(
                project.getOrganizationId(), projectId, saved.getTagName(), MdcHelper.getCorrelationId()));

        return ApiResponse.<ProjectTagDto>builder()
                .status(201)
                .message("Tag added successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> removeTag(UUID tagId) {
        log.warn("Removing tag ID: {}", tagId);

        ProjectTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new InvalidProjectOperationException("Tag not found."));

        if (tag.getProject().getStatus() == ProjectStatus.DELETED) {
            throw new InvalidProjectOperationException("Cannot remove tags from a deleted project.");
        }
        if (tag.getProject().getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedException("Cannot remove tags from an archived project.");
        }

        tagRepository.delete(tag);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Tag removed successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProjectTagDto>> getTags(UUID projectId) {
        List<ProjectTag> list = tagRepository.findByProjectId(projectId);
        List<ProjectTagDto> content = list.stream().map(this::mapToDto).collect(Collectors.toList());

        return ApiResponse.<List<ProjectTagDto>>builder()
                .status(200)
                .message("Project tags list fetched.")
                .data(content)
                .build();
    }

    private ProjectTagDto mapToDto(ProjectTag t) {
        if (t == null) return null;
        return ProjectTagDto.builder()
                .id(t.getId())
                .projectId(t.getProject().getId())
                .tagName(t.getTagName())
                .color(t.getColor())
                .build();
    }
}
