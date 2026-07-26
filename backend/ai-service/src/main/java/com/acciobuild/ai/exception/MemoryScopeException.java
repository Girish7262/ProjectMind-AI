package com.acciobuild.ai.exception;

/**
 * Exception thrown when a memory operation violates scope rules.
 */
public class MemoryScopeException extends RuntimeException {
    public MemoryScopeException(String message) {
        super(message);
    }
}
