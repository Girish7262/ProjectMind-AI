package com.acciobuild.ai.exception;

/**
 * Exception thrown when a prompt template name conflict is detected.
 */
public class DuplicatePromptTemplateException extends RuntimeException {
    public DuplicatePromptTemplateException(String message) {
        super(message);
    }
}
