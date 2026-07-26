-- AccioBuild Organization Service: Add settings limits and feature flags columns

ALTER TABLE organization_settings
    ADD COLUMN knowledge_base_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN project_module_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN document_upload_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN api_access_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN team_management_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN audit_logs_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN max_storage_gb INT NOT NULL DEFAULT 10,
    ADD COLUMN max_api_requests_per_day INT NOT NULL DEFAULT 5000,
    ADD COLUMN allowed_file_size INT NOT NULL DEFAULT 50,
    ADD COLUMN allowed_file_types VARCHAR(200) NOT NULL DEFAULT 'pdf,docx,png,jpeg,txt',
    ADD COLUMN default_language VARCHAR(10) NOT NULL DEFAULT 'en',
    ADD COLUMN default_timezone VARCHAR(100) NOT NULL DEFAULT 'UTC';
