package com.acciobuild.organization.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.security.JwtUtils;
import com.acciobuild.organization.dto.OrganizationDto;
import com.acciobuild.organization.dto.OrganizationRequest;
import com.acciobuild.organization.service.OrganizationService;
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
 * REST API Integration tests validating Request models, constraints, and controller filters.
 */
@WebMvcTest(OrganizationController.class)
public class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganizationService organizationService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "admin@acciobuild.com", roles = {"ADMIN"})
    void testCreateOrganization_Success() throws Exception {
        OrganizationRequest request = OrganizationRequest.builder()
                .organizationCode("accio-labs")
                .organizationName("Accio Labs Corp")
                .displayName("Accio Labs")
                .country("United States")
                .timezone("EST")
                .build();

        UUID id = UUID.randomUUID();
        OrganizationDto responseDto = OrganizationDto.builder()
                .id(id)
                .organizationCode("accio-labs")
                .organizationName("Accio Labs Corp")
                .displayName("Accio Labs")
                .status("ACTIVE")
                .build();

        ApiResponse<OrganizationDto> serviceRes = ApiResponse.<OrganizationDto>builder()
                .status(201)
                .message("Created successfully.")
                .data(responseDto)
                .build();

        when(organizationService.createOrganization(any(OrganizationRequest.class), any())).thenReturn(serviceRes);

        mockMvc.perform(post("/api/v1/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.organizationCode").value("accio-labs"));
    }

    @Test
    @WithMockUser(username = "admin@acciobuild.com", roles = {"ADMIN"})
    void testCreateOrganization_ValidationFailure() throws Exception {
        // Invalid request containing empty properties
        OrganizationRequest request = OrganizationRequest.builder()
                .organizationCode("")
                .organizationName("")
                .displayName("")
                .build();

        mockMvc.perform(post("/api/v1/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrganization_UnauthorizedFailure() throws Exception {
        OrganizationRequest request = OrganizationRequest.builder()
                .organizationCode("accio-labs")
                .organizationName("Accio Labs Corp")
                .displayName("Accio Labs")
                .country("USA")
                .timezone("EST")
                .build();

        // Performs call without WithMockUser context credentials
        mockMvc.perform(post("/api/v1/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "member@acciobuild.com", roles = {"MEMBER"})
    void testGetOrganizationDetails_Success() throws Exception {
        UUID orgId = UUID.randomUUID();
        OrganizationDto dto = OrganizationDto.builder()
                .id(orgId)
                .organizationCode("accio-labs")
                .organizationName("Accio Labs Corp")
                .status("ACTIVE")
                .build();

        ApiResponse<OrganizationDto> serviceRes = ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Fetched.")
                .data(dto)
                .build();

        when(organizationService.getOrganizationById(orgId)).thenReturn(serviceRes);

        mockMvc.perform(get("/api/v1/organizations/{id}", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(orgId.toString()));
    }
}
