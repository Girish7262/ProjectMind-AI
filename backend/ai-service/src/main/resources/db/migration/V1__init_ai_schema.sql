-- V1__init_ai_schema.sql
-- Flyway Database Schema Initializer for AccioBuild AI Service

CREATE TABLE ai_conversations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    project_id UUID NOT NULL,
    title VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    model_provider VARCHAR(50) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    temperature DOUBLE PRECISION,
    system_instruction VARCHAR(4000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_conversation_messages (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    content VARCHAR(8000),
    prompt_tokens INT,
    completion_tokens INT,
    total_tokens INT,
    response_duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_citations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    message_id UUID NOT NULL REFERENCES ai_conversation_messages(id) ON DELETE CASCADE,
    citation_type VARCHAR(50) NOT NULL,
    source_id UUID,
    title VARCHAR(255),
    url VARCHAR(2048),
    snippet VARCHAR(4000),
    start_index INT,
    end_index INT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_prompt_templates (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_prompt_versions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    template_id UUID NOT NULL REFERENCES ai_prompt_templates(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    system_instruction VARCHAR(4000),
    user_template VARCHAR(4000),
    parameters_json VARCHAR(2000),
    is_active BOOLEAN NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_contexts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    query_text VARCHAR(2000),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_context_sources (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    context_id UUID NOT NULL REFERENCES ai_contexts(id) ON DELETE CASCADE,
    source_type VARCHAR(50) NOT NULL,
    source_id UUID,
    content VARCHAR(8000),
    score DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_provider_configurations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    config_name VARCHAR(255) NOT NULL,
    endpoint_url VARCHAR(1000),
    api_key_vault_ref VARCHAR(255),
    default_model VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_tool_definitions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    parameter_schema_json VARCHAR(4000),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_tool_executions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    message_id UUID NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    arguments_json VARCHAR(4000),
    response_json VARCHAR(4000),
    status VARCHAR(50) NOT NULL,
    execution_duration_ms BIGINT,
    error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE ai_conversation_memories (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    memory_scope VARCHAR(50) NOT NULL,
    memory_key VARCHAR(255) NOT NULL,
    memory_value VARCHAR(8000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Optimize queries with indexes on frequently filtered/joined fields and tenant scopes
CREATE INDEX idx_ai_conv_tenant ON ai_conversations(organization_id);
CREATE INDEX idx_ai_conv_proj ON ai_conversations(project_id);
CREATE INDEX idx_ai_msg_conv ON ai_conversation_messages(conversation_id);
CREATE INDEX idx_ai_cite_msg ON ai_citations(message_id);
CREATE INDEX idx_ai_ver_tmpl ON ai_prompt_versions(template_id);
CREATE INDEX idx_ai_src_ctx ON ai_context_sources(context_id);
CREATE INDEX idx_ai_mem_conv ON ai_conversation_memories(conversation_id);
