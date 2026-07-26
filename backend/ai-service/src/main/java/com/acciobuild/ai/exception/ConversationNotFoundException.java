package com.acciobuild.ai.exception;

/**
 * Exception thrown when a requested AI conversation cannot be found.
 */
public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(String message) {
        super(message);
    }
}
