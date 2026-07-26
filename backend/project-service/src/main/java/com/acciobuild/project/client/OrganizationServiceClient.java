package com.acciobuild.project.client;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.OrganizationDto;
import com.acciobuild.project.dto.SettingsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Feign client communication proxy mapping endpoints exposed by Organization Service.
 */
@FeignClient(name = "organization-service", url = "${app.services.organization-service.url:http://localhost:8082}")
public interface OrganizationServiceClient {

    /**
     * Retrieves organization profile details.
     */
    @GetMapping("/api/v1/organizations/{organizationId}")
    ApiResponse<OrganizationDto> getOrganizationById(@PathVariable("organizationId") UUID organizationId);

    /**
     * Retrieves settings parameters of the organization.
     */
    @GetMapping("/api/v1/organizations/{organizationId}/settings")
    ApiResponse<SettingsDto> getSettings(@PathVariable("organizationId") UUID organizationId);
}
