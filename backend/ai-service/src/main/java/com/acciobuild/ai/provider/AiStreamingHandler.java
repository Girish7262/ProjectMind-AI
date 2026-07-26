package com.acciobuild.ai.provider;

/**
 * Interface supporting stream response listener hooks for chunk increments,
 * cancellations, and overall completion summaries.
 */
public interface AiStreamingHandler {
    void onChunk(String chunkText);
    void onComplete(AiResponse response);
    void onError(Throwable throwable);
    
    /**
     * Optional hook triggered when a stream is cancelled.
     */
    default void onCancel() {}
}
