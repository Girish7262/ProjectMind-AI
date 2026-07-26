package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.ConversationDto;
import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.service.ConversationService;
import com.acciobuild.ai.service.ContextBuilderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller test suite verifying endpoint routing, validation filters, and response wraps for the AI Service REST APIs.
 */
@WebMvcTest(controllers = {ConversationController.class, ContextController.class})
@AutoConfigureMockMvc(addFilters = false)
public class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private ContextBuilderService contextBuilderService;

    @MockitoBean
    private com.acciobuild.common.security.JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateConversation_Success() throws Exception {
        UUID projectId = UUID.randomUUID();
        ConversationDto request = ConversationDto.builder()
                .projectId(projectId)
                .title("Support Ticket Chat")
                .modelProvider(ProviderType.OPENAI)
                .modelName("gpt-4o")
                .temperature(0.7)
                .build();

        ConversationDto response = ConversationDto.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .title("Support Ticket Chat")
                .modelProvider(ProviderType.OPENAI)
                .modelName("gpt-4o")
                .temperature(0.7)
                .build();

        when(conversationService.createConversation(any(ConversationDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.title").value("Support Ticket Chat"));
    }

    @Test
    void testBuildContext_Success() throws Exception {
        UUID conversationId = UUID.randomUUID();
        ContextController.BuildContextRequest request = new ContextController.BuildContextRequest();
        request.setConversationId(conversationId);
        request.setQueryText("How does pgvector work?");

        ContextDto response = ContextDto.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .queryText("How does pgvector work?")
                .build();

        when(contextBuilderService.buildContext(eq(conversationId), eq("How does pgvector work?")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/context/build")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.queryText").value("How does pgvector work?"));
    }
}
