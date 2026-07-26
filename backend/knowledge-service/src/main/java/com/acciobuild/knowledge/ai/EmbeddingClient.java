package com.acciobuild.knowledge.ai;

import java.util.List;

/**
 * Interface mapping to core Spring AI embedding capabilities.
 */
public interface EmbeddingClient {

    /**
     * Compute a vector embedding representation of the given text segment.
     */
    List<Double> embed(String text);

    /**
     * Batch process list of texts and return list of vector representations.
     */
    List<List<Double>> embedBatch(List<String> texts);
}
