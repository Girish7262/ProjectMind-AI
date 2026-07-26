package com.acciobuild.ai.client;

import com.acciobuild.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;

/**
 * Feign client proxy mapping endpoints exposed by the Knowledge Service.
 */
@FeignClient(name = "knowledge-service", url = "${app.services.knowledge-service.url:http://localhost:8084}")
public interface KnowledgeServiceClient {

    /**
     * Gets details for a document.
     */
    @GetMapping("/api/v1/knowledge/documents/{documentId}")
    ApiResponse<Object> getDocumentById(@PathVariable("documentId") UUID documentId);

    /**
     * Performs RAG semantic/search query.
     */
    @GetMapping("/api/v1/knowledge/documents/search")
    ApiResponse<List<Object>> searchDocuments(
            @RequestParam("query") String query,
            @RequestParam("projectId") UUID projectId);
}
