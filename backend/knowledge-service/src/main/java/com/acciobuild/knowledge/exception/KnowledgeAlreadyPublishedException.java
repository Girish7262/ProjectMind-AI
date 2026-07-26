package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when trying to publish a document that is already published.
 */
public class KnowledgeAlreadyPublishedException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public KnowledgeAlreadyPublishedException(String message) {
        super(message, "KNOWLEDGE_ALREADY_PUBLISHED");
    }
}
