package com.acciobuild.knowledge.ai.impl;

import com.acciobuild.knowledge.ai.EmbeddingClient;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stub implementation of EmbeddingClient returning randomized vector patterns.
 */
@Component
public class StubEmbeddingClient implements EmbeddingClient {

    @Override
    public List<Double> embed(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        // Generate mock 1536-dimensional vector values
        List<Double> vector = new ArrayList<>(1536);
        for (int i = 0; i < 1536; i++) {
            vector.add(Math.random());
        }
        return vector;
    }

    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        if (texts == null) {
            return Collections.emptyList();
        }
        List<List<Double>> batch = new ArrayList<>();
        for (String text : texts) {
            batch.add(embed(text));
        }
        return batch;
    }
}
