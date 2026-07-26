package com.acciobuild.ai.provider;

import com.acciobuild.ai.enums.ProviderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying provider factory mapping and exception triggers.
 */
@ExtendWith(MockitoExtension.class)
public class AiProviderFactoryTest {

    @Mock
    private AiProviderRegistry registry;

    @InjectMocks
    private AiProviderFactory factory;

    @Test
    void testGetProviderInstanceSuccessfully() {
        AiProvider mockProvider = mock(AiProvider.class);
        when(registry.getProvider(ProviderType.OPENAI)).thenReturn(mockProvider);

        AiProvider resolved = factory.getProviderInstance(ProviderType.OPENAI);

        assertNotNull(resolved);
        assertEquals(mockProvider, resolved);
    }

    @Test
    void testGetProviderInstanceThrowsOnNull() {
        when(registry.getProvider(ProviderType.ANTHROPIC)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            factory.getProviderInstance(ProviderType.ANTHROPIC);
        });
    }
}
