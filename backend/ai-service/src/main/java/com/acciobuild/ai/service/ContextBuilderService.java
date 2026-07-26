package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.ContextDto;
import java.util.UUID;

/**
 * Service Contract for dynamic context assemblies and prompt ingestion pipelines.
 */
public interface ContextBuilderService {
    ContextDto buildContext(UUID conversationId, String queryText);
    ContextDto getContextByConversation(UUID conversationId);
}
