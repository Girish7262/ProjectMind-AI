package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a document operation violates business rules.
 */
public class InvalidKnowledgeOperationException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public InvalidKnowledgeOperationException(String message) {
        super(message, "INVALID_KNOWLEDGE_OPERATION");
    }
}
