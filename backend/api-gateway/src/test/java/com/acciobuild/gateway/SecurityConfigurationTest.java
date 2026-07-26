package com.acciobuild.gateway;

import com.acciobuild.common.security.JwtUtils;
import com.acciobuild.gateway.filter.JwtAuthenticationFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests asserting JWT signatures validations and context mappings in JwtAuthenticationFilter.
 */
@ExtendWith(MockitoExtension.class)
public class SecurityConfigurationTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private WebFilterChain chain;

    @Test
    void testJwtAuthenticationFilterSuccess() {
        String token = "valid-token";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );

        when(jwtUtils.validateToken(token)).thenReturn(true);
        
        Claims claims = new DefaultClaims(Map.of("sub", "user123", "role", "ADMIN", "tenantId", "tenant-abc"));
        when(jwtUtils.parseClaims(token)).thenReturn(claims);
        when(chain.filter(any())).thenReturn(Mono.empty());

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils);
        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        assertEquals(claims, exchange.getAttribute("claims"));
        assertEquals("user123", exchange.getAttribute("userId"));
    }
}
