-- AccioBuild Knowledge Service: Search Index & AI Chunks Schema

CREATE TABLE knowledge_search_index (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    search_text TEXT NOT NULL,
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_search_index_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE TABLE knowledge_document_chunks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    token_count INT NOT NULL,
    organization_id UUID NOT NULL,
    CONSTRAINT fk_chunk_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE TABLE document_index_metadata (
    document_id UUID PRIMARY KEY,
    chunk_count INT NOT NULL DEFAULT 0,
    estimated_token_count INT NOT NULL DEFAULT 0,
    content_hash VARCHAR(64) NOT NULL,
    embedding_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    embedding_version INT NOT NULL DEFAULT 1,
    embedding_provider VARCHAR(50),
    embedding_model VARCHAR(50),
    embedding_generated_at TIMESTAMP,
    CONSTRAINT fk_index_metadata_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_search_index_doc ON knowledge_search_index(document_id);
CREATE INDEX idx_chunks_doc ON knowledge_document_chunks(document_id);
CREATE INDEX idx_chunks_org ON knowledge_document_chunks(organization_id);
