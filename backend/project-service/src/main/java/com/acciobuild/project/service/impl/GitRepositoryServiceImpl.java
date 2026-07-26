package com.acciobuild.project.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.connector.GitProviderConnector;
import com.acciobuild.project.domain.event.RepositoryDeletedEvent;
import com.acciobuild.project.domain.event.RepositoryRegisteredEvent;
import com.acciobuild.project.domain.event.RepositorySyncCompletedEvent;
import com.acciobuild.project.domain.model.GitRepository;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectSettings;
import com.acciobuild.project.domain.model.RepositorySyncHistory;
import com.acciobuild.project.domain.repository.GitRepositoryRepository;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.domain.repository.ProjectSettingsRepository;
import com.acciobuild.project.domain.repository.RepositorySyncHistoryRepository;
import com.acciobuild.project.dto.GitRepositoryDto;
import com.acciobuild.project.enums.GitProvider;
import com.acciobuild.project.enums.SyncStatus;
import com.acciobuild.project.exception.DuplicateProjectException;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.service.GitRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing Git Repository metadata registrations, connection tests,
 * and sync logs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitRepositoryServiceImpl implements GitRepositoryService {

    private final GitRepositoryRepository gitRepositoryRepository;
    private final ProjectRepository projectRepository;
    private final ProjectSettingsRepository settingsRepository;
    private final RepositorySyncHistoryRepository syncHistoryRepository;
    private final List<GitProviderConnector> connectors;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<GitRepositoryDto> registerRepository(UUID projectId, GitRepositoryDto dto) {
        log.info("Registering repo URL: {} to project: {}", dto.getRepositoryUrl(), projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found.", "PROJECT_NOT_FOUND"));

        // Validate duplicates
        if (gitRepositoryRepository.existsByProjectIdAndRepositoryUrl(projectId, dto.getRepositoryUrl())) {
            throw new DuplicateProjectException("Repository URL has already been registered inside this project.");
        }

        // Validate limits
        ProjectSettings settings = settingsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project settings not found.", "SETTINGS_NOT_FOUND"));
        long currentCount = gitRepositoryRepository.findByProjectId(projectId).size();
        if (currentCount >= settings.getMaxRepositories()) {
            throw new InvalidProjectOperationException("Exceeded maximum connected repositories limit for this project.");
        }

        UUID repoId = UUID.randomUUID();
        GitRepository repo = new GitRepository();
        repo.setId(repoId);
        repo.setProject(project);
        repo.setProvider(GitProvider.valueOf(dto.getProvider().toUpperCase().trim()));
        repo.setRepositoryName(dto.getRepositoryName());
        repo.setRepositoryUrl(dto.getRepositoryUrl());
        repo.setDefaultBranch(dto.getDefaultBranch() != null ? dto.getDefaultBranch() : "main");
        repo.setStatus("ACTIVE");
        repo.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "PRIVATE");
        repo.setArchived(false);

        GitRepository saved = gitRepositoryRepository.save(repo);

        eventPublisher.publishEvent(new RepositoryRegisteredEvent(
                project.getOrganizationId(), projectId, repoId, saved.getRepositoryUrl(), MdcHelper.getCorrelationId()));

        return ApiResponse.<GitRepositoryDto>builder()
                .status(201)
                .message("Repository registered successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<GitRepositoryDto> updateRepository(UUID repositoryId, GitRepositoryDto dto) {
        log.info("Updating repository metadata for ID: {}", repositoryId);
        GitRepository repo = gitRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found.", "REPO_NOT_FOUND"));

        repo.setRepositoryName(dto.getRepositoryName());
        repo.setDefaultBranch(dto.getDefaultBranch());
        repo.setVisibility(dto.getVisibility());
        repo.setArchived(dto.isArchived());

        GitRepository saved = gitRepositoryRepository.save(repo);

        return ApiResponse.<GitRepositoryDto>builder()
                .status(200)
                .message("Repository updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> deleteRepository(UUID repositoryId) {
        log.warn("Deleting repository registration ID: {}", repositoryId);
        GitRepository repo = gitRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found.", "REPO_NOT_FOUND"));

        gitRepositoryRepository.delete(repo);

        eventPublisher.publishEvent(new RepositoryDeletedEvent(
                repo.getProject().getOrganizationId(), repo.getProject().getId(), MdcHelper.getCorrelationId()));

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Repository deleted successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> syncRepository(UUID repositoryId) {
        log.info("Initiating sync for repository ID: {}", repositoryId);
        GitRepository repo = gitRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found.", "REPO_NOT_FOUND"));

        RepositorySyncHistory history = new RepositorySyncHistory();
        history.setId(UUID.randomUUID());
        history.setRepository(repo);
        history.setSyncStatus(SyncStatus.RUNNING);
        history.setStartedAt(LocalDateTime.now());
        RepositorySyncHistory savedHistory = syncHistoryRepository.save(history);

        try {
            // Find connector strategy
            GitProviderConnector connector = connectors.stream()
                    .filter(c -> c.supports(repo.getProvider()))
                    .findFirst()
                    .orElse(null);

            if (connector != null) {
                connector.syncMetadata(repo);
            } else {
                log.warn("No pluggable connector connector registered forprovider: {}. Performing dry sync.", repo.getProvider());
            }

            savedHistory.setSyncStatus(SyncStatus.SUCCESS);
            savedHistory.setCompletedAt(LocalDateTime.now());
            savedHistory.setCommitCount(15); // mock commit updates
            savedHistory.setBranchCount(2);  // mock branch count
            syncHistoryRepository.save(savedHistory);

            repo.setLastSyncedAt(LocalDateTime.now());
            gitRepositoryRepository.save(repo);

            eventPublisher.publishEvent(new RepositorySyncCompletedEvent(
                    repo.getProject().getOrganizationId(), repositoryId, 15, MdcHelper.getCorrelationId()));

        } catch (Exception e) {
            log.error("Failed to synchronize repository ID: {}", repositoryId, e);
            savedHistory.setSyncStatus(SyncStatus.FAILED);
            savedHistory.setCompletedAt(LocalDateTime.now());
            savedHistory.setErrorMessage(e.getMessage());
            syncHistoryRepository.save(savedHistory);
        }

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Sync execution completed.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<GitRepositoryDto>> getRepositories(UUID projectId) {
        List<GitRepository> list = gitRepositoryRepository.findByProjectId(projectId);
        List<GitRepositoryDto> content = list.stream().map(this::mapToDto).collect(Collectors.toList());

        return ApiResponse.<List<GitRepositoryDto>>builder()
                .status(200)
                .message("Project repositories fetched.")
                .data(content)
                .build();
    }

    private GitRepositoryDto mapToDto(GitRepository r) {
        if (r == null) return null;
        return GitRepositoryDto.builder()
                .id(r.getId())
                .projectId(r.getProject().getId())
                .provider(r.getProvider().name())
                .repositoryName(r.getRepositoryName())
                .repositoryUrl(r.getRepositoryUrl())
                .defaultBranch(r.getDefaultBranch())
                .status(r.getStatus())
                .visibility(r.getVisibility())
                .isArchived(r.isArchived())
                .lastSyncedAt(r.getLastSyncedAt())
                .build();
    }
}
