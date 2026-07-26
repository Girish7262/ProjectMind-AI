# Entity Definitions

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Database Design / Entity Definitions |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

# Executive Summary

This Entity Definitions document establishes the logical specifications for all data models in the ProjectMind AI platform. For each of the thirteen primary business entities, this document outlines the purpose, owner service, attributes, relationships, business rules, lifecycle transitions, and audit properties.

By defining these properties in a language-independent format, this document serves as the primary technical reference for engineers creating Java JPA entity classes, designing relational tables, and configuring PostgreSQL pgvector storages.

---

# Entity Definition Template

The following sections define each core entity in the ProjectMind AI platform:

## User
### Purpose
Represents an individual user profile registered to access the platform.
### Owner Service
Authentication Service
### Description
Stores authentication credentials, name, email verification logs, and account status indicators.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `email` | String | Yes | Primary user email address used as login credential. | Format check; global unique constraint. |
| `passwordHash` | String | Yes | Cryptographic hash of user password. | Must be hashed using bcrypt algorithm. |
| `name` | String | Yes | Full name of the user. | Length between 2 and 100 characters. |
| `status` | String | Yes | Active status (Active, Inactive, Suspended). | Restricted to predefined status list. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **OrganizationMember** | One-to-Many | Maps user to organizations and role groups. |
| **AIConversation** | One-to-Many | Maps user to their Q&A lookup history threads. |
| **AuditLog** | One-to-Many | Tracks security actions triggered by user session. |
### Business Rules
* The email must be verified before account status shifts to Active.
* A user account can be deactivated, which halts active JWT session validations immediately.
### Lifecycle
* *Creation:* User registers with credentials; status defaults to Inactive until email activation token is validated.
* *Update:* User modifies profile name or updates password hash.
* *Archive:* Deactivating sets the `deleted` flag to `true` and user status to Suspended.
* *Delete:* Hard delete is performed only by compliance officers during GDPR forget-me audits.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## Role
### Purpose
Defines a security privilege set within the platform.
### Owner Service
Authentication Service
### Description
Stores predefined role descriptions (Engineer, Tech Lead, Admin) that define user permissions limits.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `name` | String | Yes | Predefined role name (Engineer, Admin). | Unique constraint. |
| `description` | String | No | Summary of permissions assigned. | Max 255 characters. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **OrganizationMember** | One-to-Many | Assigns role permissions to tenant memberships. |
### Business Rules
* Predefined roles cannot be deleted or renamed, preserving security configurations.
### Lifecycle
* *Creation:* Initialized during database migrations.
* *Update:* Modifying description details via admin controls.
* *Archive:* Deactivation is restricted; roles are persistent assets.
* *Delete:* Hard delete is blocked.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## Organization
### Purpose
Represents an isolated corporate tenant organization.
### Owner Service
Organization Service
### Description
Defines the parent boundaries for users, projects, configurations, and billing parameters.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `name` | String | Yes | Legal name of the corporate customer. | Length 2 to 100 characters. |
| `domainSuffix` | String | Yes | Suffix domain (e.g. company.com) to restrict email registrations. | Unique constraint; suffix syntax validation. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **OrganizationMember** | One-to-Many | Hosts member users profiles. |
| **Project** | One-to-Many | Hosts project workspaces. |
### Business Rules
* Registration is gated by the verified corporate email domain suffix constraint.
* Tenant isolation is enforced logically.
### Lifecycle
* *Creation:* Initialized by global admins during tenant onboarding.
* *Update:* Modify org name or update domain suffix restrictions.
* *Archive:* Deactivating sets the `deleted` flag to `true`, disabling login access for all organization users.
* *Delete:* Hard delete is blocked unless tenant data retention terms expire.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## OrganizationMember
### Purpose
Associates a User profile with a tenant Organization and Role.
### Owner Service
Organization Service
### Description
Acts as the mapping entity tracking membership status and permissions for a user within a corporate tenant.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `organizationId` | UUID | Yes | Target organization tenant foreign key. | Must match active organization ID. |
| `userId` | UUID | Yes | Target user foreign key. | Must match active user ID. |
| `roleId` | UUID | Yes | Target role permissions foreign key. | Must match active role ID. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **Organization** | Many-to-One | Parent tenant organization. |
| **User** | Many-to-One | Member user profile. |
| **Role** | Many-to-One | Assigned security role permissions. |
### Business Rules
* A user must belong to exactly one tenant organization.
* A user can hold only one active role within the organization.
### Lifecycle
* *Creation:* User accepts invitation link and joins the organization.
* *Update:* Administrator modifies user role assignment.
* *Archive:* Setting `deleted` flag to `true` revokes user access to the tenant.
* *Delete:* Removed during workspace cleanup.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## Project
### Purpose
Establishes a project workspace boundary to group codebase connectors.
### Owner Service
Project Service
### Description
Serves as the workspace boundary grouping code repositories, Jira projects, and Confluence spaces.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `name` | String | Yes | Name of the project workspace. | Unique within the tenant boundary. |
| `namespace` | String | Yes | Unique text URL handle for routing. | Unique globally; alphanumeric characters only. |
| `organizationId` | UUID | Yes | Parent organization tenant. | Must reference valid organization. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **Organization** | Many-to-One | Parent tenant organization. |
| **KnowledgeSource** | One-to-Many | Links codebase and tickets connectors. |
### Business Rules
* A project workspace must belong to exactly one tenant organization.
* Archiving a project disables search queries against its connected files.
### Lifecycle
* *Creation:* Project is created by an organization admin.
* *Update:* Admin updates project description or settings.
* *Archive:* Archiving flags the project as deleted, stopping background index updates.
* *Delete:* Deleted during index cleanups.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## KnowledgeSource
### Purpose
Manages connection settings and credentials for external integrations.
### Owner Service
Knowledge Service
### Description
Stores API connection endpoints, read-only OAuth tokens, and synchronization schedules.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `type` | String | Yes | Source system type (GitHub, Jira, Confluence). | Restricted to predefined connector types. |
| `tokenHash` | String | Yes | Encrypted read-only API access token. | AES-256 encrypted string format. |
| `syncStatus` | String | Yes | Status indicator (Connected, Syncing, Offline). | Restricted to status list. |
| `projectId` | UUID | Yes | Parent project workspace. | Must reference valid project. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **Project** | Many-to-One | Parent project workspace. |
| **Repository** | One-to-Many | Maps specific code repositories. |
| **Document** | One-to-Many | Maps ingested files and pages. |
### Business Rules
* Source connectors are strictly read-only, preventing code modifications.
* Credentials must be encrypted using keys unique to each organization.
### Lifecycle
* *Creation:* Connector is added to project settings.
* *Update:* Admin rotates API tokens or updates schedules.
* *Archive:* Deactivating sets `deleted` to `true`, stopping index syncs.
* *Delete:* Removed during cleanup.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## Repository
### Purpose
Tracks connected source repositories synced from GitHub.
### Owner Service
Knowledge Service
### Description
Stores code repository metadata, paths, and git log histories.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `name` | String | Yes | Name of the repository. | Unique within the connector scope. |
| `url` | String | Yes | Git URL of the repository. | Format validation URL syntax. |
| `knowledgeSourceId` | UUID | Yes | Parent knowledge source connector. | Must reference active connector. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **KnowledgeSource** | Many-to-One | Parent knowledge source connector. |
### Business Rules
* Repository paths must be unique within a project workspace.
### Lifecycle
* *Creation:* Synced during initial GitHub connector setup.
* *Update:* Modify repository details or branch targets.
* *Archive:* Deactivating sets the `deleted` flag to `true`, removing files from search logic.
* *Delete:* Hard deleted when projects are removed.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## Document
### Purpose
Represents an individual file or page ingested from target tools.
### Owner Service
AI Service
### Description
Stores metadata pointing to ingested codebase files, Jira stories, or Confluence articles.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `name` | String | Yes | File name or wiki title. | Max 255 characters. |
| `path` | String | Yes | Absolute path or URL of the source. | URL or file path formatting. |
| `knowledgeSourceId` | UUID | Yes | Parent connector source. | Must reference active connector. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **KnowledgeSource** | Many-to-One | Parent connector source. |
| **KnowledgeChunk** | One-to-Many | Segmented text content chunks. |
### Business Rules
* Ingested documents are read-only database records.
* Outdated documents are re-indexed during sync runs.
### Lifecycle
* *Creation:* Created by background sync workers.
* *Update:* Updated when code commits update target file contents.
* *Archive:* Soft deleted if the file is removed from git.
* *Delete:* Cleaned up during index runs.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## KnowledgeChunk
### Purpose
Stores parsed text segments extracted from documents.
### Owner Service
AI Service
### Description
Splits large text files into tokenized segments to support vector embeddings.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `textContent` | String | Yes | Raw text extracted from the document. | Max chunk character limits. |
| `documentId` | UUID | Yes | Parent ingested document. | Must reference active document. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **Document** | Many-to-One | Parent ingested document. |
| **Embedding** | One-to-One | Vector embedding representation. |
### Business Rules
* Chunks are read-only records linked to their source documents.
### Lifecycle
* *Creation:* Created during document parsing runs.
* *Update:* Updated when parent document changes.
* *Archive:* Deactivated when parent document is soft-deleted.
* *Delete:* Hard deleted during index updates.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## Embedding
### Purpose
Stores the vector embedding representation of a text chunk.
### Owner Service
AI Service
### Description
Holds the vector array used for similarity search queries in pgvector.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `vectorData` | Vector | Yes | Mathematical vector representing chunk semantics. | Dimension check matching model output (e.g. 1536). |
| `knowledgeChunkId` | UUID | Yes | Parent knowledge chunk. | Must reference active chunk. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **KnowledgeChunk** | One-to-One | Parent text chunk. |
### Business Rules
* Every chunk has exactly one vector embedding.
* Vector arrays must align with the configured model's dimensions.
### Lifecycle
* *Creation:* Generated during AI indexing.
* *Update:* Regenerated when parent chunk text changes.
* *Archive:* Deactivated when parent chunk is deleted.
* *Delete:* Hard deleted during index updates.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## AIConversation
### Purpose
Tracks chat history threads created by developers.
### Owner Service
Search Service
### Description
Groups related Q&A messages into active threads for developer reference.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `title` | String | Yes | Title of the conversation thread. | Max 100 characters. |
| `userId` | UUID | Yes | User who initiated the conversation. | Must reference active user. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **User** | Many-to-One | Creator profile. |
| **AIMessage** | One-to-Many | Chronological log of Q&A messages. |
### Business Rules
* Conversations are private to the creator user profile.
### Lifecycle
* *Creation:* Initialized when a user submits a new query.
* *Update:* User modifies conversation title.
* *Archive:* Deactivating sets `deleted` to `true`, removing it from the user list.
* *Delete:* Removed after log retention periods expire.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## AIMessage
### Purpose
Represents individual messages inside a conversation thread.
### Owner Service
Search Service
### Description
Stores developer question text and corresponding context-grounded AI answers with citations.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `role` | String | Yes | Message role (Developer, System, AI). | Restricted to predefined roles list. |
| `content` | String | Yes | Text content of the message. | Parameter validation for prompt injections. |
| `citations` | String | No | JSON list of referenced file paths and ticket IDs. | JSON syntax format verification. |
| `conversationId` | UUID | Yes | Parent conversation thread. | Must reference active conversation. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **AIConversation** | Many-to-One | Parent conversation thread. |
### Business Rules
* Messages are immutable once generated.
* Generated responses must base their facts on indexed codebase context.
### Lifecycle
* *Creation:* Added during Q&A processing.
* *Update:* Blocked (immutable logs).
* *Archive:* Soft deleted when parent conversation is deactivated.
* *Delete:* Hard deleted after retention periods expire.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

## AuditLog
### Purpose
Maintains an immutable security log of administrative changes and query activities.
### Owner Service
Administration Service
### Description
Stores system logs to support security audits and verification.
### Attributes
| Attribute | Data Type | Required | Description | Validation |
|---|---|---|---|---|
| `action` | String | Yes | Action performed (Login, Query, Token Rotation). | Predefined action types validation. |
| `ipAddress` | String | Yes | Source IP address. | IP format validation. |
| `userId` | UUID | Yes | User who triggered the action. | Must reference active user. |
### Relationships
| Related Entity | Relationship | Description |
|---|---|---|
| **User** | Many-to-One | Triggering user profile. |
### Business Rules
* Audit logs are append-only and immutable.
### Lifecycle
* *Creation:* Written automatically by security interceptors.
* *Update:* Blocked (read-only records).
* *Archive:* Blocked (must remain active).
* *Delete:* Purged after 365 days retention terms expire.
### Audit Fields
Includes `id` (UUID), `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version`, and `deleted`.

---

# Entity Validation Rules

Validation constraints govern entity changes to preserve data integrity:

* **Mandatory Fields:** Fields such as user emails, passwords, names, domains, primary keys, and organization identifiers must be non-nullable.
* **Optional Fields:** Non-critical description details, citation metadata, and tracking logs can accept null values.
* **Unique Constraints:** The system enforces uniqueness on user emails, domains, project namespaces, and repository paths.
* **Business Validations:** Input fields are validated against regex checks (e.g. valid domain formats, correct repository URL paths).
* **Status Transitions:** Users transition from Inactive (signup) to Active (verified email) or Suspended (deactivated). Knowledge sources cycle through Connected, Syncing, and Offline based on connection success.

---

# Entity Ownership Matrix

The microservices ownership boundaries are mapped below:

| Entity Name | Owner Microservice | Used By (Read Access) |
|---|---|---|
| **Organization** | Organization Service | Auth Service, Project Service |
| **User** | Authentication Service | Org Service, Search Service, Administration |
| **Role** | Authentication Service | Org Service |
| **OrganizationMember** | Organization Service | Auth Service, Administration |
| **Project** | Project Service | Knowledge Service, Search Service |
| **KnowledgeSource** | Knowledge Service | AI Service, Administration |
| **Repository** | Knowledge Service | AI Service |
| **Document** | AI Service | Search Service |
| **KnowledgeChunk** | AI Service | Search Service |
| **Embedding** | AI Service | Search Service |
| **AIConversation** | Search Service | Frontend Apps |
| **AIMessage** | Search Service | Frontend Apps |
| **AuditLog** | Administration Service | Compliance Officers |

---

# Entity Dependency Matrix

The logical schema dependencies are mapped below:

| Entity Name | Depends On | Referenced By |
|---|---|---|
| **Organization** | None | OrganizationMember, Project |
| **User** | None | OrganizationMember, AIConversation, AuditLog |
| **Role** | None | OrganizationMember |
| **OrganizationMember** | Organization, User, Role | None |
| **Project** | Organization | KnowledgeSource |
| **KnowledgeSource** | Project | Repository, Document |
| **Repository** | KnowledgeSource | None |
| **Document** | KnowledgeSource | KnowledgeChunk |
| **KnowledgeChunk** | Document | Embedding |
| **Embedding** | KnowledgeChunk | None |
| **AIConversation** | User | AIMessage |
| **AIMessage** | AIConversation | None |
| **AuditLog** | User | None |

---

# Design Principles

ProjectMind AI's entity design adheres to the following principles:

* **High Cohesion:** Every entity encapsulates attributes representing a single logical concept (e.g., Project only manages workspace configurations).
* **Low Coupling:** Cross-service data lookups rely on decoupled user roles mapping, minimizing transactional connection locks.
* **Normalization:** relational schemas are normalized to 3NF, minimizing data redundancy.
* **Referential Integrity:** Enforced using explicit Foreign Key checks with ON DELETE RESTRICT on core tables, preventing cascade data loss.
* **Auditability:** Every entity features auditing timestamps (`createdAt`, `updatedAt`, `version`) to track modifications.
* **Scalability:** Decoupling vector database structures (Embeddings) from metadata tables allows high-volume indexing configurations to scale independently.

---

# Risks

Entity modeling faces key database integrity risks:

### Data Duplication
* *Risk:* Code commits duplicate raw text blocks in the search database, inflating storage costs.
* *Mitigation:* Split documents into clean, non-overlapping chunks, using unique hashes to update changes.

### Orphan Records
* *Risk:* Deactivating parent project settings leaves orphaned chunks or embeddings in the vector database.
* *Mitigation:* Enforce logical database deletes, updating child tables when a parent entity is soft-deleted.

### Invalid Relationships
* *Risk:* A project workspace is linked to a connector belonging to a different organization.
* *Mitigation:* Enforce multi-tenancy validation checks at the application repository layer.

---

# Conclusion

The Entity Definitions document establishes the logical specifications for ProjectMind AI's data models. By defining attributes, cardinalities, validations, service ownership, and lifecycles, this specification prepares the the developer to implement JPA repositories and construct secure, scalable databases.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Entity Definitions Document. |
