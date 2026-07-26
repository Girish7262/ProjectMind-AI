package com.acciobuild.knowledge.enums;

/**
 * Standard embedding models supported for vector extraction.
 */
public enum EmbeddingModel {
    TEXT_EMBEDDING_3_SMALL("text-embedding-3-small", 1536),
    TEXT_EMBEDDING_3_LARGE("text-embedding-3-large", 3072),
    TEXT_EMBEDDING_ADA_002("text-embedding-ada-002", 1536),
    TITAN_EMBED_TEXT("amazon.titan-embed-text-v1", 1536),
    VERTEX_TEXT_EMBEDDING("text-embedding-004", 768),
    OLLAMA_NOMAD("nomic-embed-text", 768);

    private final String value;
    private final int defaultDimension;

    EmbeddingModel(String value, int defaultDimension) {
        this.value = value;
        this.defaultDimension = defaultDimension;
    }

    public String getValue() {
        return value;
    }

    public int getDefaultDimension() {
        return defaultDimension;
    }
}
