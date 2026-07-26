package com.acciobuild.ai.provider;

import com.acciobuild.ai.enums.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory creating or mapping provider types to target adapters.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiProviderFactory {

    private final AiProviderRegistry registry;

    public AiProvider getProviderInstance(ProviderType type) {
        log.info("Resolving provider instance for type: {}", type);
        AiProvider provider = registry.getProvider(type);
        if (provider == null) {
            throw new IllegalArgumentException("No provider registered for type: " + type);
        }
        return provider;
    }
}
