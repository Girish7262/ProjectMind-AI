-- AccioBuild Organization Service Database Initialization Schema

-- 1. Organizations table (Base entity representing SaaS tenants)
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    organization_code VARCHAR(50) NOT NULL,
    organization_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    logo_url VARCHAR(255),
    website VARCHAR(255),
    industry VARCHAR(100),
    organization_size VARCHAR(50),
    country VARCHAR(100) NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_organizations_code UNIQUE (organization_code),
    CONSTRAINT uk_organizations_name UNIQUE (organization_name)
);

-- Indexes for organizations code and name lookups
CREATE INDEX idx_organizations_code ON organizations(organization_code);
CREATE INDEX idx_organizations_name ON organizations(organization_name);

-- 2. Organization Settings table (One-To-One mapping with sharing PK ID constraint)
CREATE TABLE organization_settings (
    organization_id UUID PRIMARY KEY,
    ai_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    knowledge_sharing_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    max_projects INT NOT NULL DEFAULT 10,
    max_members INT NOT NULL DEFAULT 50,
    CONSTRAINT fk_organization_settings_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE CASCADE
);

-- 3. Organization Members table (Many-to-Many user membership mapping table)
CREATE TABLE organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uk_organization_members_org_user UNIQUE (organization_id, user_id),
    CONSTRAINT fk_organization_members_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE CASCADE
);

-- Indexes for organization membership searches
CREATE INDEX idx_organization_members_org ON organization_members(organization_id);
CREATE INDEX idx_organization_members_user ON organization_members(user_id);

-- 4. Organization Invitations table (Invites tracking logic)
CREATE TABLE organization_invitations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    email VARCHAR(150) NOT NULL,
    invite_token VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    invited_by UUID NOT NULL,
    CONSTRAINT uk_organization_invitations_token UNIQUE (invite_token),
    CONSTRAINT fk_organization_invitations_organization FOREIGN KEY (organization_id)
        REFERENCES organizations(id) ON DELETE CASCADE
);

-- Indexes for invitations tracking
CREATE INDEX idx_organization_invitations_token ON organization_invitations(invite_token);
CREATE INDEX idx_organization_invitations_org ON organization_invitations(organization_id);
CREATE INDEX idx_organization_invitations_email ON organization_invitations(email);
