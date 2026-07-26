package com.acciobuild.gateway;

import com.acciobuild.gateway.config.RateLimiterConfig;
import com.acciobuild.gateway.domain.event.GatewayHealthChangedEvent;
import com.acciobuild.gateway.health.GatewayHealthIndicator;
import com.acciobuild.gateway.health.AuthServiceHealthIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying Gateway Health metrics, rate limiter IP resolving, and domain events.
 */
public class ObservabilityHealthAndLimitTest {

    @Test
    void testGatewayHealthIndicator() {
        GatewayHealthIndicator indicator = new GatewayHealthIndicator();
        Health health = indicator.health();
        assertNotNull(health);
        assertEquals("UP", health.getStatus().getCode());
        assertTrue(health.getDetails().containsKey("freeMemory"));
    }

    @Test
    void testAuthServiceHealthIndicator() {
        AuthServiceHealthIndicator indicator = new AuthServiceHealthIndicator();
        Mono<Health> healthMono = indicator.health();
        StepVerifier.create(healthMono)
                .assertNext(health -> {
                    assertEquals("UP", health.getStatus().getCode());
                    assertEquals("Auth Service", health.getDetails().get("service"));
                })
                .verifyComplete();
    }

    @Test
    void testIpKeyResolverExtractsRemoteIp() {
        RateLimiterConfig config = new RateLimiterConfig();
        KeyResolver ipResolver = config.ipKeyResolver();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects")
                        .remoteAddress(new InetSocketAddress("192.168.1.100", 80))
        );

        Mono<String> ipMono = ipResolver.resolve(exchange);
        StepVerifier.create(ipMono)
                .expectNext("192.168.1.100")
                .verifyComplete();
    }

    @Test
    void testDomainEventsCreation() {
        UUID orgId = UUID.randomUUID();
        GatewayHealthChangedEvent event = new GatewayHealthChangedEvent(
                orgId, "Auth Service", "DOWN", "UP", "corr-123"
        );

        assertEquals("HEALTH_CHANGED", event.getEventType());
        assertEquals(orgId, event.getOrganizationId());
        assertEquals("corr-123", event.getCorrelationId());
        assertEquals("Auth Service", event.getServiceName());
        assertEquals("DOWN", event.getOldStatus());
        assertEquals("UP", event.getNewStatus());
        assertNotNull(event.getTimestamp());
    }
}
