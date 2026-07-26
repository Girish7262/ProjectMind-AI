-- AccioBuild Knowledge Service: AI Readiness & Embedding Schema

ALTER TABLE knowledge_document_chunks
    ADD COLUMN estimated_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    ADD COLUMN language VARCHAR(10) NOT NULL DEFAULT 'en',
    ADD COLUMN priority INT NOT NULL DEFAULT 0,
    ADD COLUMN chunk_hash VARCHAR(64) NOT NULL DEFAULT '',
    ADD COLUMN content_checksum VARCHAR(64) NOT NULL DEFAULT '',
    ADD COLUMN embedding_eligibility BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE document_index_metadata
    ADD COLUMN vector_id UUID,
    ADD COLUMN embedding_dimension INT,
    ADD COLUMN embedding_checksum VARCHAR(64),
    ADD COLUMN embedding_updated_at TIMESTAMP;

CREATE TABLE embedding_jobs (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    estimated_tokens INT NOT NULL DEFAULT 0,
    estimated_cost DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_embedding_job_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE TABLE embedding_provider_config (
    id UUID PRIMARY KEY,
    provider_name VARCHAR(50) NOT NULL UNIQUE,
    api_key_vault_ref VARCHAR(100) NOT NULL,
    endpoint_url VARCHAR(250) NOT NULL,
    default_model VARCHAR(50) NOT NULL,
    default_dimension INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE embedding_processing_log (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    step_name VARCHAR(50) NOT NULL,
    duration_ms BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    message VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_log_job FOREIGN KEY (job_id) REFERENCES embedding_jobs(id) ON DELETE CASCADE
);

CREATE INDEX idx_embedding_job_status ON embedding_jobs(status);
