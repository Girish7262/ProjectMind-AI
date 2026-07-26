package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.MemoryExpiredEvent;
import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.domain.repository.AiConversationMemoryRepository;
import com.acciobuild.ai.service.MemoryRetentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation enforcing count and time-to-live policies on stored memories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryRetentionServiceImpl implements MemoryRetentionService {

    private final AiConversationMemoryRepository memoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int CONVERSATION_MAX_KEYS = 20;
    private static final int PROJECT_RETENTION_DAYS = 30;
    private static final int ORG_RETENTION_DAYS = 90;
    private static final int GLOBAL_RETENTION_DAYS = 180;

    @Override
    @Transactional
    public void enforceRetentionForConversation(UUID conversationId) {
        log.info("Enforcing retention checks for conversation memory: {}", conversationId);
        List<AiConversationMemory> memories = memoryRepository.findByConversationId(conversationId);
        
        if (memories.size() > CONVERSATION_MAX_KEYS) {
            memories.sort(Comparator.comparing(AiConversationMemory::getUpdatedAt));
            int excess = memories.size() - CONVERSATION_MAX_KEYS;
            for (int i = 0; i < excess; i++) {
                AiConversationMemory toDelete = memories.get(i);
                memoryRepository.delete(toDelete);
                
                eventPublisher.publishEvent(new MemoryExpiredEvent(
                        toDelete.getOrganizationId(),
                        toDelete.getConversationId(),
                        toDelete.getMemoryScope().name(),
                        toDelete.getMemoryKey(),
                        UUID.randomUUID().toString()
                ));
                log.info("Pruned excess conversation memory key '{}' for conversation '{}'", toDelete.getMemoryKey(), conversationId);
            }
        }
    }

    @Override
    @Transactional
    public void enforceRetentionForProject(UUID projectId) {
        log.info("Enforcing retention checks for project scope memory: {}", projectId);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(PROJECT_RETENTION_DAYS);
        pruneMemoriesOlderThan(cutoff, "PROJECT");
    }

    @Override
    @Transactional
    public void enforceRetentionForOrganization(UUID organizationId) {
        log.info("Enforcing retention checks for organization: {}", organizationId);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(ORG_RETENTION_DAYS);
        pruneMemoriesOlderThan(cutoff, "ORGANIZATION");
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs daily at midnight
    @Override
    @Transactional
    public void executeScheduledCleanup() {
        log.info("Executing scheduled automatic cleanup for AI Memory store.");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(GLOBAL_RETENTION_DAYS);
        pruneMemoriesOlderThan(cutoff, "GLOBAL");
    }

    private void pruneMemoriesOlderThan(LocalDateTime cutoff, String policyType) {
        List<AiConversationMemory> allMemories = memoryRepository.findAll();
        for (AiConversationMemory mem : allMemories) {
            if (mem.getUpdatedAt().isBefore(cutoff)) {
                memoryRepository.delete(mem);
                eventPublisher.publishEvent(new MemoryExpiredEvent(
                        mem.getOrganizationId(),
                        mem.getConversationId(),
                        mem.getMemoryScope().name(),
                        mem.getMemoryKey(),
                        UUID.randomUUID().toString()
                ));
                log.info("Pruned memory key '{}' based on {} policy. Cutoff: {}", mem.getMemoryKey(), policyType, cutoff);
            }
        }
    }
}
