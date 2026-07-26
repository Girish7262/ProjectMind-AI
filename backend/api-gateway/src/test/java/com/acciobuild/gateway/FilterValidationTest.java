package com.acciobuild.gateway;

import com.acciobuild.gateway.filter.CorrelationIdFilter;
import com.acciobuild.gateway.filter.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

/**
 * Unit tests verifying reactive header injections in CorrelationIdFilter and RequestIdFilter.
 */
public class FilterValidationTest {

    @Test
    void testCorrelationIdFilterInjectsHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects")
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        CorrelationIdFilter filter = new CorrelationIdFilter();
        
        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain, times(1)).filter(captor.capture());
        ServerWebExchange mutated = captor.getValue();

        String correlationId = mutated.getRequest().getHeaders().getFirst("X-Correlation-Id");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());

        String respHeader = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
        assertEquals(correlationId, respHeader);
    }

    @Test
    void testRequestIdFilterInjectsHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/projects")
        );

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        RequestIdFilter filter = new RequestIdFilter();

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain, times(1)).filter(captor.capture());
        ServerWebExchange mutated = captor.getValue();

        String requestId = mutated.getRequest().getHeaders().getFirst("X-Request-Id");
        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
    }
}
