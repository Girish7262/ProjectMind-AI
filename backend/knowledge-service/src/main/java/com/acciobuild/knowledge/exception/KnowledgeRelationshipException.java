package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a circular relationship or relation error is detected.
 */
public class KnowledgeRelationshipException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public KnowledgeRelationshipException(String message) {
        super(message, "KNOWLEDGE_RELATIONSHIP_ERROR");
    }
}
