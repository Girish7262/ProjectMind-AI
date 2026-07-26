package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a lifecycle state transition violates state rules.
 */
public class InvalidKnowledgeStateException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public InvalidKnowledgeStateException(String message) {
        super(message, "INVALID_KNOWLEDGE_STATE");
    }
}
