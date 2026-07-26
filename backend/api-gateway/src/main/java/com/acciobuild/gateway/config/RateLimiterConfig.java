package com.acciobuild.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Configuration class exposing KeyResolvers for IP, User, and Tenant rate limiting.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getAttribute("userId");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    @Bean
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
            if (tenantId == null) {
                io.jsonwebtoken.Claims claims = exchange.getAttribute("claims");
                if (claims != null) {
                    tenantId = claims.get("tenantId", String.class);
                    if (tenantId == null) {
                        tenantId = claims.get("organizationId", String.class);
                    }
                }
            }
            return Mono.just(tenantId != null ? tenantId : "global");
        };
    }
}
