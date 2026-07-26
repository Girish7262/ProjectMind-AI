package com.acciobuild.ai.exception;

/**
 * Exception thrown when a conversation is in an invalid state for the requested operation.
 */
public class InvalidConversationStateException extends RuntimeException {
    public InvalidConversationStateException(String message) {
        super(message);
    }
}
