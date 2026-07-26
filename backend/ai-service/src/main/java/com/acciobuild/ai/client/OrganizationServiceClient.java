package com.acciobuild.ai.client;

import com.acciobuild.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Feign client proxy mapping endpoints exposed by the Organization Service.
 */
@FeignClient(name = "organization-service", url = "${app.services.organization-service.url:http://localhost:8082}")
public interface OrganizationServiceClient {

    /**
     * Verifies organization existence and details.
     */
    @GetMapping("/api/v1/organizations/{orgId}")
    ApiResponse<Object> getOrganizationById(@PathVariable("orgId") UUID orgId);
}
