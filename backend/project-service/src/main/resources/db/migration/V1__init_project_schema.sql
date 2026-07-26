-- AccioBuild Project Service Database Schema Initializer

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    project_code VARCHAR(50) NOT NULL,
    project_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_project_code UNIQUE (project_code),
    CONSTRAINT uq_project_name_org UNIQUE (organization_id, project_name)
);

CREATE TABLE project_settings (
    project_id UUID PRIMARY KEY,
    ai_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    knowledge_capture_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    code_analysis_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    documentation_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    max_repositories INT NOT NULL DEFAULT 5,
    default_branch VARCHAR(50) NOT NULL DEFAULT 'main',
    CONSTRAINT fk_project_settings_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE project_members (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_member_user UNIQUE (project_id, user_id)
);

CREATE TABLE project_tags (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    color VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    CONSTRAINT fk_project_tag_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_tag_name UNIQUE (project_id, tag_name)
);

-- Performance and tenant query indexes
CREATE INDEX idx_project_org_status ON projects(organization_id, status);
CREATE INDEX idx_project_members_user_role ON project_members(user_id, role);
CREATE INDEX idx_project_tags_name ON project_tags(tag_name);
