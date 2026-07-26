package com.acciobuild.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic routing configuration mapping microservice contexts.
 */
@Configuration
public class RouteConfiguration {

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(100, 200);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, 
                                           GatewayProperties properties,
                                           org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter,
                                           org.springframework.cloud.gateway.filter.ratelimit.KeyResolver ipKeyResolver) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("authCircuitBreaker").setFallbackUri("forward:/fallback/auth"))
                                .requestRateLimiter(l -> l.setRateLimiter(redisRateLimiter).setKeyResolver(ipKeyResolver))
                        )
                        .uri(properties.getAuthServiceUri()))
                .route("organization-service", r -> r.path("/api/v1/organizations/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("orgCircuitBreaker").setFallbackUri("forward:/fallback/organization"))
                                .requestRateLimiter(l -> l.setRateLimiter(redisRateLimiter).setKeyResolver(ipKeyResolver))
                        )
                        .uri(properties.getOrganizationServiceUri()))
                .route("project-service", r -> r.path("/api/v1/projects/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("projectCircuitBreaker").setFallbackUri("forward:/fallback/project"))
                                .requestRateLimiter(l -> l.setRateLimiter(redisRateLimiter).setKeyResolver(ipKeyResolver))
                        )
                        .uri(properties.getProjectServiceUri()))
                .route("knowledge-service", r -> r.path("/api/v1/knowledge/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("knowledgeCircuitBreaker").setFallbackUri("forward:/fallback/knowledge"))
                                .requestRateLimiter(l -> l.setRateLimiter(redisRateLimiter).setKeyResolver(ipKeyResolver))
                        )
                        .uri(properties.getKnowledgeServiceUri()))
                .route("ai-service", r -> r.path("/api/v1/ai/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("aiCircuitBreaker").setFallbackUri("forward:/fallback/ai"))
                                .requestRateLimiter(l -> l.setRateLimiter(redisRateLimiter).setKeyResolver(ipKeyResolver))
                        )
                        .uri(properties.getAiServiceUri()))
                .build();
    }
}
