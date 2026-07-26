package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a requested document version log is not found.
 */
public class KnowledgeVersionNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs the exception.
     */
    public KnowledgeVersionNotFoundException(String message) {
        super(message, "KNOWLEDGE_VERSION_NOT_FOUND");
    }
}
