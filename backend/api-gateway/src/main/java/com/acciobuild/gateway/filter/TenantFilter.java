package com.acciobuild.gateway.filter;

import com.acciobuild.common.constant.HeaderConstants;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter propagating Tenant ID and User ID downstream.
 */
@Component
@Slf4j
public class TenantFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Claims claims = exchange.getAttribute("claims");
        String userId = exchange.getAttribute("userId");

        if (claims != null && userId != null) {
            String tenantId = claims.get("tenantId", String.class);
            if (tenantId == null) {
                tenantId = claims.get("organizationId", String.class);
            }

            if (tenantId != null) {
                log.debug("Injecting downstream tenant headers: TenantId={}, UserId={}", tenantId, userId);
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header(HeaderConstants.TENANT_ID, tenantId)
                        .header(HeaderConstants.USER_ID, userId)
                        .build();
                exchange = exchange.mutate().request(mutatedRequest).build();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
