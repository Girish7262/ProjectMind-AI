package com.acciobuild.ai.exception;

/**
 * Exception thrown when context gathering or ranking fails.
 */
public class ContextBuildException extends RuntimeException {
    public ContextBuildException(String message) {
        super(message);
    }
}
