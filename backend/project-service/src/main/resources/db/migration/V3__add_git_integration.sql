-- AccioBuild Project Service: Git Repositories and Integration Schema

CREATE TABLE git_repositories (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    provider VARCHAR(20) NOT NULL,
    repository_name VARCHAR(100) NOT NULL,
    repository_url VARCHAR(250) NOT NULL,
    default_branch VARCHAR(50) NOT NULL DEFAULT 'main',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_git_repo_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_git_repo_url_project UNIQUE (project_id, repository_url)
);

CREATE TABLE repository_credentials (
    repository_id UUID PRIMARY KEY,
    credential_type VARCHAR(20) NOT NULL,
    encrypted_token VARCHAR(500),
    username VARCHAR(100),
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_repo_cred_repo FOREIGN KEY (repository_id) REFERENCES git_repositories(id) ON DELETE CASCADE
);

CREATE TABLE repository_webhooks (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL,
    webhook_url VARCHAR(250) NOT NULL,
    secret VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_triggered_at TIMESTAMP,
    CONSTRAINT fk_repo_webhook_repo FOREIGN KEY (repository_id) REFERENCES git_repositories(id) ON DELETE CASCADE
);

CREATE TABLE repository_sync_history (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    commit_count INT DEFAULT 0,
    branch_count INT DEFAULT 0,
    error_message VARCHAR(500),
    CONSTRAINT fk_repo_sync_repo FOREIGN KEY (repository_id) REFERENCES git_repositories(id) ON DELETE CASCADE
);

-- Performance and query indexes
CREATE INDEX idx_git_repo_project ON git_repositories(project_id);
CREATE INDEX idx_repo_sync_repo ON repository_sync_history(repository_id);
