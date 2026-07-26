package com.acciobuild.ai.service;

import java.util.UUID;

/**
 * Service orchestrating cleanup tasks and boundary limits on stored memories.
 */
public interface MemoryRetentionService {
    void enforceRetentionForConversation(UUID conversationId);
    void enforceRetentionForProject(UUID projectId);
    void enforceRetentionForOrganization(UUID organizationId);
    void executeScheduledCleanup();
}
