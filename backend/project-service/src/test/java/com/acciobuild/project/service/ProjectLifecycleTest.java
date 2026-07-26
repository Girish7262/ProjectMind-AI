package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.dto.ProjectDto;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.InvalidProjectStateException;
import com.acciobuild.project.scheduler.CleanupScheduler;
import com.acciobuild.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Project state machine, lifecycle transitions, and schedulers.
 */
@ExtendWith(MockitoExtension.class)
public class ProjectLifecycleTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ProjectServiceImpl projectService;
    @InjectMocks private CleanupScheduler cleanupScheduler;

    private UUID projectId;
    private Project project;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        project.setOrganizationId(UUID.randomUUID());
        project.setProjectCode("blockchain-core");
        project.setProjectName("Blockchain Core");
        project.setStatus(ProjectStatus.PLANNING);
    }

    @Test
    void testTransition_PlanningToActive_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ApiResponse<ProjectDto> response = projectService.updateProjectStatus(projectId, "ACTIVE");

        assertNotNull(response);
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void testTransition_PlanningToArchived_Failure() {
        // PLANNING to ARCHIVED is an invalid state transition
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(InvalidProjectStateException.class, () -> {
            projectService.updateProjectStatus(projectId, "ARCHIVED");
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void testSchedulerAutoArchival_Success() {
        project.setStatus(ProjectStatus.ACTIVE);
        // Set update timestamp to be older than 180 days limit
        project.setUpdatedAt(LocalDateTime.now().minusDays(200));

        when(projectRepository.findAll()).thenReturn(Collections.singletonList(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        cleanupScheduler.executeAutoArchival();

        assertEquals(ProjectStatus.ARCHIVED, project.getStatus());
        verify(projectRepository).save(project);
    }
}
