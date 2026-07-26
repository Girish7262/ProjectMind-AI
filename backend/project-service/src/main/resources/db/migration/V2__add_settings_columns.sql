-- AccioBuild Project Service: Add settings limits and feature flags columns

ALTER TABLE project_settings
    ADD COLUMN repository_sync_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN webhooks_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN cicd_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN api_access_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN audit_logging_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN max_documents INT NOT NULL DEFAULT 100,
    ADD COLUMN max_team_members INT NOT NULL DEFAULT 20,
    ADD COLUMN storage_limit_gb INT NOT NULL DEFAULT 5,
    ADD COLUMN daily_ai_requests INT NOT NULL DEFAULT 100,
    ADD COLUMN webhook_url VARCHAR(250),
    ADD COLUMN allowed_repository_providers VARCHAR(100) NOT NULL DEFAULT 'github,gitlab',
    ADD COLUMN code_analysis_profile VARCHAR(50) NOT NULL DEFAULT 'standard',
    ADD COLUMN documentation_template VARCHAR(50) NOT NULL DEFAULT 'default';
