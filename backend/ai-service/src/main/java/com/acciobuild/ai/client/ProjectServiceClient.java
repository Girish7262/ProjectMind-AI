package com.acciobuild.ai.client;

import com.acciobuild.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Feign client proxy mapping endpoints exposed by the Project Service.
 */
@FeignClient(name = "project-service", url = "${app.services.project-service.url:http://localhost:8083}")
public interface ProjectServiceClient {

    /**
     * Verifies project existence and details.
     */
    @GetMapping("/api/v1/projects/{projectId}")
    ApiResponse<Object> getProjectById(@PathVariable("projectId") UUID projectId);
}
