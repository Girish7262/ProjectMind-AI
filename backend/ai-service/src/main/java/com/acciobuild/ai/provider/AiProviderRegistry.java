package com.acciobuild.ai.provider;

import com.acciobuild.ai.domain.event.ProviderActivatedEvent;
import com.acciobuild.ai.domain.event.ProviderDeactivatedEvent;
import com.acciobuild.ai.domain.event.ProviderRegisteredEvent;
import com.acciobuild.ai.enums.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry holding and managing model providers, priorities, and health check state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiProviderRegistry {

    private final List<AiProvider> springProviders;
    private final ApplicationEventPublisher eventPublisher;

    private final Map<ProviderType, AiProvider> providers = new ConcurrentHashMap<>();
    private final Map<ProviderType, Boolean> activeStatus = new ConcurrentHashMap<>();
    private final Map<ProviderType, Integer> priorities = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing AiProviderRegistry with available Spring provider beans");
        for (AiProvider p : springProviders) {
            registerProvider(p, p.getDefaultPriority(), true);
        }
    }

    public synchronized void registerProvider(AiProvider provider, int priority, boolean active) {
        log.info("Registering provider: {}, priority: {}, active: {}", provider.getName(), priority, active);
        providers.put(provider.getType(), provider);
        priorities.put(provider.getType(), priority);
        activeStatus.put(provider.getType(), active);

        eventPublisher.publishEvent(new ProviderRegisteredEvent(
                UUID.randomUUID(),
                provider.getName(),
                provider.getDiscoverableModels().isEmpty() ? "unknown" : provider.getDiscoverableModels().get(0),
                UUID.randomUUID().toString()
        ));
    }

    public synchronized void unregisterProvider(ProviderType type) {
        log.info("Unregistering provider: {}", type);
        AiProvider p = providers.remove(type);
        priorities.remove(type);
        activeStatus.remove(type);

        if (p != null) {
            eventPublisher.publishEvent(new ProviderDeactivatedEvent(UUID.randomUUID(), p.getName(), UUID.randomUUID().toString()));
        }
    }

    public AiProvider getProvider(ProviderType type) {
        return providers.get(type);
    }

    public List<AiProvider> getActiveProviders() {
        List<AiProvider> activeList = new ArrayList<>();
        for (Map.Entry<ProviderType, AiProvider> entry : providers.entrySet()) {
            if (Boolean.TRUE.equals(activeStatus.get(entry.getKey())) && entry.getValue().isHealthy()) {
                activeList.add(entry.getValue());
            }
        }
        // Sort by priority descending
        activeList.sort((p1, p2) -> Integer.compare(getPriority(p2.getType()), getPriority(p1.getType())));
        return activeList;
    }

    public boolean isActive(ProviderType type) {
        return Boolean.TRUE.equals(activeStatus.getOrDefault(type, false));
    }

    public void setStatus(ProviderType type, boolean active) {
        activeStatus.put(type, active);
        AiProvider p = getProvider(type);
        if (p != null) {
            if (active) {
                eventPublisher.publishEvent(new ProviderActivatedEvent(UUID.randomUUID(), p.getName(), UUID.randomUUID().toString()));
            } else {
                eventPublisher.publishEvent(new ProviderDeactivatedEvent(UUID.randomUUID(), p.getName(), UUID.randomUUID().toString()));
            }
        }
    }

    public int getPriority(ProviderType type) {
        return priorities.getOrDefault(type, 0);
    }

    public void setPriority(ProviderType type, int priority) {
        priorities.put(type, priority);
    }
}
