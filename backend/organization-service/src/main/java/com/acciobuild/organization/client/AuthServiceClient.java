package com.acciobuild.organization.client;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

/**
 * Feign Client for integration with the Auth Service.
 * Retrieves and validates user details using HTTP REST requests.
 */
@FeignClient(name = "auth-service", url = "${app.auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    /**
     * Resolves user metadata matching unique ID parameter.
     */
    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") UUID id);

    /**
     * Resolves user metadata matching unique email parameter.
     */
    @GetMapping("/api/v1/users/by-email")
    ApiResponse<UserResponse> getUserByEmail(@RequestParam("email") String email);
}
