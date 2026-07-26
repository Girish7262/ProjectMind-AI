package com.acciobuild.ai.exception;

/**
 * Exception thrown when tool parameter definition or run validation fails.
 */
public class ToolExecutionException extends RuntimeException {
    public ToolExecutionException(String message) {
        super(message);
    }
}
