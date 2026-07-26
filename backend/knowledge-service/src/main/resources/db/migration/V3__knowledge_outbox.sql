-- AccioBuild Knowledge Service: Transactional Outbox Schema

CREATE TABLE knowledge_outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INT NOT NULL DEFAULT 1,
    tenant_id UUID NOT NULL,
    project_id UUID,
    payload_json TEXT NOT NULL,
    headers_json TEXT,
    correlation_id VARCHAR(100),
    causation_id VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_knowledge_outbox_status ON knowledge_outbox_events(status, created_at);
