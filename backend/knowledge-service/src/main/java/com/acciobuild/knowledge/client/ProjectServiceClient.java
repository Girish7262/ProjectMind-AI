package com.acciobuild.knowledge.client;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.ProjectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Feign client proxy mapping endpoints exposed by Project Service.
 */
@FeignClient(name = "project-service", url = "${app.services.project-service.url:http://localhost:8083}")
public interface ProjectServiceClient {

    /**
     * Retrieves remote project workspace details by ID reference.
     */
    @GetMapping("/api/v1/projects/{projectId}")
    ApiResponse<ProjectDto> getProjectById(@PathVariable("projectId") UUID projectId);
}
