package com.acciobuild.project.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.security.JwtUtils;
import com.acciobuild.project.dto.ProjectDto;
import com.acciobuild.project.dto.ProjectRequest;
import com.acciobuild.project.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST API Integration tests validating Request models, constraints, and controllers for Project Service.
 */
@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "maintainer@acciobuild.com", roles = {"MAINTAINER"})
    void testCreateProject_Success() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .projectCode("compiler")
                .projectName("LLVM Compiler Backend")
                .displayName("LLVM Backend")
                .visibility("PRIVATE")
                .build();

        UUID id = UUID.randomUUID();
        ProjectDto responseDto = ProjectDto.builder()
                .id(id)
                .projectCode("compiler")
                .projectName("LLVM Compiler Backend")
                .status("PLANNING")
                .build();

        ApiResponse<ProjectDto> serviceRes = ApiResponse.<ProjectDto>builder()
                .status(201)
                .message("Created successfully.")
                .data(responseDto)
                .build();

        when(projectService.createProject(any(ProjectRequest.class), any(), any())).thenReturn(serviceRes);

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.projectCode").value("compiler"));
    }

    @Test
    @WithMockUser(username = "maintainer@acciobuild.com", roles = {"MAINTAINER"})
    void testCreateProject_ValidationFailure() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .projectCode("")
                .projectName("")
                .visibility("")
                .build();

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateProject_UnauthorizedFailure() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .projectCode("compiler")
                .projectName("LLVM Compiler Backend")
                .visibility("PRIVATE")
                .build();

        mockMvc.perform(post("/api/v1/projects")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "developer@acciobuild.com", roles = {"DEVELOPER"})
    void testGetProjectDetails_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectDto dto = ProjectDto.builder()
                .id(projectId)
                .projectCode("compiler")
                .status("ACTIVE")
                .build();

        ApiResponse<ProjectDto> serviceRes = ApiResponse.<ProjectDto>builder()
                .status(200)
                .message("Fetched.")
                .data(dto)
                .build();

        when(projectService.getProjectById(projectId)).thenReturn(serviceRes);

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(projectId.toString()));
    }
}
