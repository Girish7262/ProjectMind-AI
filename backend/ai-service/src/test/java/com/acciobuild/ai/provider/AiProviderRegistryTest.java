package com.acciobuild.ai.provider;

import com.acciobuild.ai.enums.ProviderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying registrations, de-registrations, priority, and event publishing in AiProviderRegistry.
 */
@ExtendWith(MockitoExtension.class)
public class AiProviderRegistryTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void testRegistryPopulatedAndStatusUpdated() {
        AiProvider p1 = mock(AiProvider.class);
        when(p1.getType()).thenReturn(ProviderType.OPENAI);
        when(p1.getName()).thenReturn("OpenAI Test");
        when(p1.getDiscoverableModels()).thenReturn(List.of("gpt-4"));
        when(p1.getDefaultPriority()).thenReturn(100);
        when(p1.isHealthy()).thenReturn(true);

        AiProviderRegistry registry = new AiProviderRegistry(List.of(p1), eventPublisher);
        registry.init();

        verify(eventPublisher, times(1)).publishEvent(any(Object.class));

        assertEquals(p1, registry.getProvider(ProviderType.OPENAI));

        List<AiProvider> active = registry.getActiveProviders();
        assertEquals(1, active.size());
        assertEquals(p1, active.get(0));

        registry.setStatus(ProviderType.OPENAI, false);
        assertFalse(registry.isActive(ProviderType.OPENAI));
        verify(eventPublisher, times(2)).publishEvent(any(Object.class));
    }
}
