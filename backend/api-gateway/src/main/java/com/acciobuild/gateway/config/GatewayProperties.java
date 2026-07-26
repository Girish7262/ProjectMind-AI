package com.acciobuild.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties class mapping downstream microservices target URLs and authorization secret keys.
 */
@Data
@Component("appGatewayProperties")
@ConfigurationProperties(prefix = "app.gateway")
public class GatewayProperties {
    private String jwtSecret = "dGhpcy1pcy1hLXNhZWZ0eS1zZWNyZXQta2V5LXdoaWNoLW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1mb3ItamN3dC1zaWduYXR1cmUtc2FmZXR5";
    private String authServiceUri = "lb://auth-service";
    private String organizationServiceUri = "lb://organization-service";
    private String projectServiceUri = "lb://project-service";
    private String knowledgeServiceUri = "lb://knowledge-service";
    private String aiServiceUri = "lb://ai-service";
}
