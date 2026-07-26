package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
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
import com.acciobuild.project.service.impl.GitRepositoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Git repository registration limits and synchronization flows.
 */
@ExtendWith(MockitoExtension.class)
public class GitRepositoryServiceTest {

    @Mock private GitRepositoryRepository repositoryRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectSettingsRepository settingsRepository;
    @Mock private RepositorySyncHistoryRepository syncHistoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private GitRepositoryServiceImpl repositoryService;
    private java.util.List<com.acciobuild.project.connector.GitProviderConnector> connectors;

    private UUID projectId;
    private Project project;
    private ProjectSettings settings;
    private GitRepositoryDto dto;

    @BeforeEach
    void setUp() {
        connectors = new java.util.ArrayList<>();
        repositoryService = new GitRepositoryServiceImpl(
                repositoryRepository,
                projectRepository,
                settingsRepository,
                syncHistoryRepository,
                connectors,
                eventPublisher
        );
        projectId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        project.setOrganizationId(UUID.randomUUID());

        settings = new ProjectSettings();
        settings.setProject(project);
        settings.setMaxRepositories(3); // Limit set to 3 connected repos

        dto = GitRepositoryDto.builder()
                .provider("GITHUB")
                .repositoryName("accio-app")
                .repositoryUrl("https://github.com/acciobuild/accio-app.git")
                .defaultBranch("main")
                .build();
    }

    @Test
    void testRegisterRepository_DuplicateUrl_Failure() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(repositoryRepository.existsByProjectIdAndRepositoryUrl(projectId, dto.getRepositoryUrl()))
                .thenReturn(true); // URL already registered

        assertThrows(DuplicateProjectException.class, () -> {
            repositoryService.registerRepository(projectId, dto);
        });

        verify(repositoryRepository, never()).save(any());
    }

    @Test
    void testRegisterRepository_LimitExceeded_Failure() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(settingsRepository.findById(projectId)).thenReturn(Optional.of(settings));

        // Create 3 pre-registered repositories to hit limit
        ArrayList<GitRepository> existing = new ArrayList<>();
        existing.add(new GitRepository());
        existing.add(new GitRepository());
        existing.add(new GitRepository());
        when(repositoryRepository.findByProjectId(projectId)).thenReturn(existing);

        assertThrows(InvalidProjectOperationException.class, () -> {
            repositoryService.registerRepository(projectId, dto);
        });
    }

    @Test
    void testSyncRepository_Success() {
        UUID repoId = UUID.randomUUID();
        GitRepository repo = new GitRepository();
        repo.setId(repoId);
        repo.setProject(project);
        repo.setProvider(GitProvider.GITHUB);

        when(repositoryRepository.findById(repoId)).thenReturn(Optional.of(repo));
        when(syncHistoryRepository.save(any(RepositorySyncHistory.class))).thenAnswer(i -> i.getArgument(0));

        ApiResponse<Void> response = repositoryService.syncRepository(repoId);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(eventPublisher).publishEvent(any(Object.class));
    }
}
