package com.acciobuild.knowledge.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a review action violates workflow constraints.
 */
public class KnowledgeReviewException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public KnowledgeReviewException(String message) {
        super(message, "KNOWLEDGE_REVIEW_ERROR");
    }
}
