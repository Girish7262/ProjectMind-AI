package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a duplicate document title or slug check fails.
 */
public class DuplicateKnowledgeDocumentException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public DuplicateKnowledgeDocumentException(String message) {
        super(message, "DUPLICATE_KNOWLEDGE_DOCUMENT");
    }
}
