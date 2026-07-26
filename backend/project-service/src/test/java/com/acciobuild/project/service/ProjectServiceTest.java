package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.client.OrganizationServiceClient;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectMember;
import com.acciobuild.project.domain.repository.ProjectMemberRepository;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.dto.OrganizationDto;
import com.acciobuild.project.dto.ProjectDto;
import com.acciobuild.project.dto.ProjectMemberDto;
import com.acciobuild.project.dto.ProjectRequest;
import com.acciobuild.project.dto.SettingsDto;
import com.acciobuild.project.enums.ProjectMemberRole;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.DuplicateProjectException;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.exception.ProjectLimitExceededException;
import com.acciobuild.project.service.impl.ProjectMemberServiceImpl;
import com.acciobuild.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Project aggregate business rules, Feign validation limits,
 * and membership rules.
 */
@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository memberRepository;
    @Mock private OrganizationServiceClient organizationServiceClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ProjectServiceImpl projectService;
    @InjectMocks private ProjectMemberServiceImpl memberService;

    private UUID organizationId;
    private UUID creatorId;
    private ProjectRequest request;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        request = ProjectRequest.builder()
                .projectCode("tele-health")
                .projectName("TeleHealth System")
                .visibility("PRIVATE")
                .build();
    }

    @Test
    void testCreateProject_LimitExceeded_Failure() {
        OrganizationDto orgDto = new OrganizationDto();
        orgDto.setId(organizationId);
        orgDto.setStatus("ACTIVE");

        SettingsDto settingsDto = new SettingsDto();
        settingsDto.setMaxProjects(3); // Organization limits set to 3 projects max

        when(organizationServiceClient.getOrganizationById(organizationId)).thenReturn(new ApiResponse<>(200, "Success", orgDto));
        when(organizationServiceClient.getSettings(organizationId)).thenReturn(new ApiResponse<>(200, "Success", settingsDto));
        when(projectRepository.countByOrganizationId(organizationId)).thenReturn(3L); // Organization already has 3 projects

        assertThrows(ProjectLimitExceededException.class, () -> {
            projectService.createProject(request, organizationId, creatorId);
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    void testCreateProject_CodeCollision_Failure() {
        OrganizationDto orgDto = new OrganizationDto();
        orgDto.setId(organizationId);
        orgDto.setStatus("ACTIVE");

        when(organizationServiceClient.getOrganizationById(organizationId)).thenReturn(new ApiResponse<>(200, "Success", orgDto));
        when(projectRepository.existsByProjectCode("tele-health")).thenReturn(true);

        assertThrows(DuplicateProjectException.class, () -> {
            projectService.createProject(request, organizationId, creatorId);
        });
    }

    @Test
    void testSoleMaintainerDemotionBlock_Failure() {
        UUID projectId = UUID.randomUUID();
        ProjectMember soleMaintainer = new ProjectMember();
        soleMaintainer.setUserId(creatorId);
        soleMaintainer.setRole(ProjectMemberRole.MAINTAINER);

        when(memberRepository.findByProjectIdAndUserId(projectId, creatorId)).thenReturn(Optional.of(soleMaintainer));
        when(memberRepository.findByProjectId(projectId)).thenReturn(Collections.singletonList(soleMaintainer));

        assertThrows(InvalidProjectOperationException.class, () -> {
            memberService.updateMemberRole(projectId, creatorId, "DEVELOPER");
        });
    }

    @Test
    void testProjectOwnershipTransfer_Success() {
        UUID projectId = UUID.randomUUID();
        
        Project project = new Project();
        project.setId(projectId);
        project.setOrganizationId(organizationId);

        ProjectMember oldOwner = new ProjectMember();
        oldOwner.setProject(project);
        oldOwner.setUserId(creatorId);
        oldOwner.setRole(ProjectMemberRole.MAINTAINER);

        UUID candidateId = UUID.randomUUID();
        ProjectMember candidate = new ProjectMember();
        candidate.setProject(project);
        candidate.setUserId(candidateId);
        candidate.setRole(ProjectMemberRole.DEVELOPER);

        when(memberRepository.findByProjectIdAndUserId(projectId, candidateId)).thenReturn(Optional.of(candidate));
        when(memberRepository.findByProjectId(projectId)).thenReturn(List.of(oldOwner, candidate));
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<ProjectMemberDto> response = memberService.updateMemberRole(projectId, candidateId, "MAINTAINER");

        assertNotNull(response);
        assertEquals(ProjectMemberRole.MAINTAINER.name(), response.getData().getRole());
        assertEquals(ProjectMemberRole.DEVELOPER, oldOwner.getRole());
        verify(memberRepository).save(oldOwner);
    }
}
