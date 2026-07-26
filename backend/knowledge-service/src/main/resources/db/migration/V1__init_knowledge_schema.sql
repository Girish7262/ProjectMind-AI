-- AccioBuild Knowledge Service Database Schema Initializer

CREATE TABLE knowledge_documents (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    summary VARCHAR(500),
    content_type VARCHAR(50) NOT NULL,
    content_format VARCHAR(50) NOT NULL DEFAULT 'markdown',
    status VARCHAR(20) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_doc_slug_project UNIQUE (project_id, slug)
);

CREATE TABLE knowledge_versions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    version_number INT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    storage_location VARCHAR(250) NOT NULL,
    change_summary VARCHAR(250),
    created_at TIMESTAMP NOT NULL,
    created_by UUID NOT NULL,
    CONSTRAINT fk_version_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE TABLE knowledge_attachments (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    file_name VARCHAR(150) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(250) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    CONSTRAINT fk_attachment_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE TABLE knowledge_metadata (
    document_id UUID PRIMARY KEY,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    keywords VARCHAR(250),
    author VARCHAR(100),
    review_status VARCHAR(20) NOT NULL,
    approval_status VARCHAR(20) NOT NULL,
    last_reviewed_at TIMESTAMP,
    CONSTRAINT fk_metadata_document FOREIGN KEY (document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE
);

CREATE TABLE knowledge_categories (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(250),
    color VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    icon VARCHAR(50) NOT NULL DEFAULT 'folder'
);

CREATE TABLE knowledge_tags (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(10) NOT NULL DEFAULT '#6366f1'
);

CREATE TABLE knowledge_collections (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    visibility VARCHAR(20) NOT NULL
);

CREATE TABLE knowledge_relationships (
    id UUID PRIMARY KEY,
    source_document_id UUID NOT NULL,
    target_document_id UUID NOT NULL,
    relationship_type VARCHAR(20) NOT NULL,
    strength DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    CONSTRAINT fk_relation_source FOREIGN KEY (source_document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_relation_target FOREIGN KEY (target_document_id) REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    CONSTRAINT uq_relation_src_tgt UNIQUE (source_document_id, target_document_id)
);

-- Performance and multi-tenant indexes
CREATE INDEX idx_doc_org_project ON knowledge_documents(organization_id, project_id);
CREATE INDEX idx_doc_status_visibility ON knowledge_documents(status, visibility);
CREATE INDEX idx_version_doc ON knowledge_versions(document_id);
CREATE INDEX idx_relation_source ON knowledge_relationships(source_document_id);
