package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a version conflict occurs during a document update.
 */
public class KnowledgeVersionConflictException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public KnowledgeVersionConflictException(String message) {
        super(message, "KNOWLEDGE_VERSION_CONFLICT");
    }
}
