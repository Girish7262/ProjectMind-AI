package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when project document limits or size caps are exceeded.
 */
public class KnowledgeLimitExceededException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public KnowledgeLimitExceededException(String message) {
        super(message, "KNOWLEDGE_LIMIT_EXCEEDED");
    }
}
