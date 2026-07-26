package com.acciobuild.ai.provider;

import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.provider.strategy.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying load balancing, failover, capability check, and fallback logic in routing strategies.
 */
@ExtendWith(MockitoExtension.class)
public class AiProviderStrategyTest {

    @Test
    void testPrimarySelectionStrategy() {
        AiProvider p1 = mock(AiProvider.class);
        PrimarySelectionStrategy strategy = new PrimarySelectionStrategy();

        AiProvider selected = strategy.selectProvider(List.of(p1), null);
        assertEquals(p1, selected);
    }

    @Test
    void testFallbackSelectionStrategyFailover() {
        AiProvider pPrimary = mock(AiProvider.class);
        when(pPrimary.getType()).thenReturn(ProviderType.OPENAI);
        when(pPrimary.isHealthy()).thenReturn(false);

        AiProvider pFallback = mock(AiProvider.class);
        when(pFallback.getType()).thenReturn(ProviderType.AZURE);
        when(pFallback.isHealthy()).thenReturn(true);

        FallbackSelectionStrategy strategy = new FallbackSelectionStrategy(ProviderType.OPENAI, ProviderType.AZURE);

        AiProvider selected = strategy.selectProvider(List.of(pPrimary, pFallback), null);
        assertEquals(pFallback, selected);
    }

    @Test
    void testRoundRobinSelectionStrategyRotates() {
        AiProvider p1 = mock(AiProvider.class);
        AiProvider p2 = mock(AiProvider.class);

        RoundRobinSelectionStrategy strategy = new RoundRobinSelectionStrategy();

        AiProvider first = strategy.selectProvider(List.of(p1, p2), null);
        AiProvider second = strategy.selectProvider(List.of(p1, p2), null);
        AiProvider third = strategy.selectProvider(List.of(p1, p2), null);

        assertNotEquals(first, second);
        assertEquals(first, third);
    }

    @Test
    void testCapabilitySelectionStrategyFilters() {
        AiProvider pChatOnly = mock(AiProvider.class);
        when(pChatOnly.supportsCapability(AiModelCapability.TOOL_CALLING)).thenReturn(false);

        AiProvider pToolCapable = mock(AiProvider.class);
        when(pToolCapable.supportsCapability(AiModelCapability.TOOL_CALLING)).thenReturn(true);

        CapabilitySelectionStrategy strategy = new CapabilitySelectionStrategy();

        AiProvider selected = strategy.selectProvider(List.of(pChatOnly, pToolCapable), AiModelCapability.TOOL_CALLING);
        assertEquals(pToolCapable, selected);
    }
}
