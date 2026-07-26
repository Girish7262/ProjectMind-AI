package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when trying to archive a document that is already archived.
 */
public class KnowledgeAlreadyArchivedException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public KnowledgeAlreadyArchivedException(String message) {
        super(message, "KNOWLEDGE_ALREADY_ARCHIVED");
    }
}
