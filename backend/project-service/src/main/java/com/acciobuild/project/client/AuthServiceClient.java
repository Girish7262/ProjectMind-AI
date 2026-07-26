package com.acciobuild.project.client;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * Feign client communication proxy mapping endpoints exposed by Auth Service.
 */
@FeignClient(name = "auth-service", url = "${app.services.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    /**
     * Retrieves user profile details by ID references.
     */
    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable("userId") UUID userId);
}
