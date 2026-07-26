package com.acciobuild.ai.engine;

import com.acciobuild.ai.client.KnowledgeServiceClient;
import com.acciobuild.ai.client.OrganizationServiceClient;
import com.acciobuild.ai.client.ProjectServiceClient;
import com.acciobuild.ai.dto.ContextDto.SourceDto;
import com.acciobuild.ai.dto.MemoryDto;
import com.acciobuild.ai.enums.ContextSourceType;
import com.acciobuild.ai.service.MemoryService;
import com.acciobuild.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Pipeline component aggregating metadata settings, memories, and document chunks in parallel.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationContextAssembler {

    private final ProjectServiceClient projectClient;
    private final OrganizationServiceClient organizationClient;
    private final KnowledgeServiceClient knowledgeClient;
    private final MemoryService memoryService;

    /**
     * Executes non-blocking parallel queries gathering context inputs.
     */
    public List<SourceDto> assemble(UUID conversationId, UUID projectId, UUID organizationId, String queryText) {
        log.info("Assembling context references for conversation: {}", conversationId);

        List<SourceDto> sources = new ArrayList<>();

        CompletableFuture<ApiResponse<Object>> orgFuture = CompletableFuture.supplyAsync(() -> 
                organizationClient.getOrganizationById(organizationId));
                
        CompletableFuture<ApiResponse<Object>> projectFuture = CompletableFuture.supplyAsync(() -> 
                projectClient.getProjectById(projectId));

        CompletableFuture<ApiResponse<List<Object>>> knowledgeFuture = CompletableFuture.supplyAsync(() -> 
                knowledgeClient.searchDocuments(queryText, organizationId));

        try {
            CompletableFuture.allOf(orgFuture, projectFuture, knowledgeFuture).join();

            ApiResponse<Object> orgRes = orgFuture.get();
            if (orgRes != null && orgRes.getData() != null) {
                sources.add(SourceDto.builder()
                        .id(UUID.randomUUID())
                        .sourceType(ContextSourceType.USER_METADATA)
                        .sourceId(organizationId)
                        .content("Organization details: " + orgRes.getData().toString())
                        .score(1.0)
                        .build());
            }

            ApiResponse<Object> projectRes = projectFuture.get();
            if (projectRes != null && projectRes.getData() != null) {
                sources.add(SourceDto.builder()
                        .id(UUID.randomUUID())
                        .sourceType(ContextSourceType.METADATA_FIELD)
                        .sourceId(projectId)
                        .content("Project settings: " + projectRes.getData().toString())
                        .score(1.0)
                        .build());
            }

            ApiResponse<List<Object>> knowledgeRes = knowledgeFuture.get();
            if (knowledgeRes != null && knowledgeRes.getData() != null) {
                List<Object> docs = knowledgeRes.getData();
                for (int i = 0; i < docs.size(); i++) {
                    sources.add(SourceDto.builder()
                            .id(UUID.randomUUID())
                            .sourceType(ContextSourceType.KNOWLEDGE_CHUNK)
                            .sourceId(UUID.randomUUID())
                            .content(docs.get(i).toString())
                            .score(0.9 - (i * 0.05))
                            .build());
                }
            }

        } catch (Exception e) {
            log.warn("Non-blocking error aggregating third-party service context: {}", e.getMessage());
        }

        try {
            List<MemoryDto> memories = memoryService.getMemoryForConversation(conversationId);
            for (MemoryDto mem : memories) {
                sources.add(SourceDto.builder()
                        .id(UUID.randomUUID())
                        .sourceType(ContextSourceType.USER_METADATA)
                        .sourceId(conversationId)
                        .content("Memory [" + mem.getMemoryKey() + "]: " + mem.getMemoryValue())
                        .score(0.85)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve memory variables for context assembler: {}", e.getMessage());
        }

        return sources;
    }
}
