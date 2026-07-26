package com.acciobuild.ai.exception;

/**
 * Exception thrown when citation validation fails.
 */
public class CitationValidationException extends RuntimeException {
    public CitationValidationException(String message) {
        super(message);
    }
}
