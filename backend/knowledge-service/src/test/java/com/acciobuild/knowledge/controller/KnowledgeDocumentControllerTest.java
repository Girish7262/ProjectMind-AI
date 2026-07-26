package com.acciobuild.knowledge.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.security.KnowledgeUserDetails;
import com.acciobuild.knowledge.service.KnowledgeDocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller test suite verifying endpoint routing, validation filters, and response wraps.
 */
@WebMvcTest(KnowledgeDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class KnowledgeDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeDocumentService documentService;

    @MockitoBean
    private com.acciobuild.common.security.JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateDocument_Success() throws Exception {
        UUID docId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        KnowledgeDocumentDto request = KnowledgeDocumentDto.builder()
                .projectId(projectId)
                .title("Deployment Guide")
                .slug("deploy-guide")
                .visibility("INTERNAL")
                .sourceType("MANUAL")
                .build();

        KnowledgeDocumentDto response = KnowledgeDocumentDto.builder()
                .id(docId)
                .projectId(projectId)
                .title("Deployment Guide")
                .slug("deploy-guide")
                .visibility("INTERNAL")
                .sourceType("MANUAL")
                .build();

        KnowledgeUserDetails mockUser = new KnowledgeUserDetails(
                UUID.randomUUID(), "editor@acciobuild.com", UUID.randomUUID(),
                Collections.singletonList(new SimpleGrantedAuthority("EDITOR"))
        );

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities())
        );

        when(documentService.createDocument(any(KnowledgeDocumentDto.class), any(UUID.class)))
                .thenReturn(new ApiResponse<>(201, "Document provisioned successfully.", response));

        try {
            mockMvc.perform(post("/api/v1/knowledge/documents")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.slug").value("deploy-guide"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testGetDocument_Success() throws Exception {
        UUID docId = UUID.randomUUID();
        KnowledgeDocumentDto response = KnowledgeDocumentDto.builder()
                .id(docId)
                .title("Deployment Guide")
                .slug("deploy-guide")
                .visibility("INTERNAL")
                .sourceType("MANUAL")
                .build();

        when(documentService.getDocument(eq(docId)))
                .thenReturn(new ApiResponse<>(200, "Document fetched.", response));

        mockMvc.perform(get("/api/v1/knowledge/documents/{documentId}", docId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.title").value("Deployment Guide"));
    }
}
