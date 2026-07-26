package com.acciobuild.ai.observability;

import com.acciobuild.ai.health.*;
import com.acciobuild.ai.provider.AiProviderRegistry;
import com.acciobuild.ai.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying PostgreSQL connection validations, Redis pings, and registered active stubs bounds in Custom health indicators.
 */
@ExtendWith(MockitoExtension.class)
public class CustomHealthIndicatorTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private RedisConnectionFactory redisConnectionFactory;
    @Mock
    private RedisConnection redisConnection;
    @Mock
    private AiProviderRegistry providerRegistry;
    @Mock
    private ToolRegistry toolRegistry;

    @Test
    void testPostgresHealthUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        PostgresHealthIndicator indicator = new PostgresHealthIndicator(dataSource);
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void testRedisHealthUp() {
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        RedisHealthIndicator indicator = new RedisHealthIndicator(redisConnectionFactory);
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void testProviderRegistryHealthDown() {
        when(providerRegistry.getActiveProviders()).thenReturn(Collections.emptyList());

        ProviderRegistryHealthIndicator indicator = new ProviderRegistryHealthIndicator(providerRegistry);
        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void testToolRegistryHealthUp() {
        when(toolRegistry.getAllTools()).thenReturn(List.of(mock(com.acciobuild.ai.tool.Tool.class)));

        ToolRegistryHealthIndicator indicator = new ToolRegistryHealthIndicator(toolRegistry);
        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
    }
}
