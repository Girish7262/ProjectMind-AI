package com.acciobuild.gateway;

import com.acciobuild.gateway.config.GatewayProperties;
import com.acciobuild.gateway.config.RouteConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests asserting dynamic programmatic routing rules configuration.
 */
public class RouteConfigurationTest {

    @Test
    void testRouteMappingsLocator() {
        GatewayProperties properties = new GatewayProperties();
        RouteLocatorBuilder builder = mock(RouteLocatorBuilder.class);
        RouteLocatorBuilder.Builder routeBuilder = mock(RouteLocatorBuilder.Builder.class);
        RouteLocator routeLocator = mock(RouteLocator.class);

        when(builder.routes()).thenReturn(routeBuilder);
        when(routeBuilder.route(anyString(), any())).thenReturn(routeBuilder);
        when(routeBuilder.build()).thenReturn(routeLocator);

        RouteConfiguration routeConfig = new RouteConfiguration();
        org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter = mock(org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter.class);
        org.springframework.cloud.gateway.filter.ratelimit.KeyResolver keyResolver = mock(org.springframework.cloud.gateway.filter.ratelimit.KeyResolver.class);
        RouteLocator locator = routeConfig.customRouteLocator(builder, properties, redisRateLimiter, keyResolver);

        assertNotNull(locator);
        verify(builder, times(1)).routes();
    }
}
