package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a requested Knowledge Document is not found.
 */
public class KnowledgeDocumentNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs the exception.
     */
    public KnowledgeDocumentNotFoundException(String message) {
        super(message, "KNOWLEDGE_DOCUMENT_NOT_FOUND");
    }
}
