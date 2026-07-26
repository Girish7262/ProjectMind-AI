# Database Schema

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Database Design / Database Schema |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

# Executive Summary

This Database Schema document defines the logical entities, relationships, attributes, indexing strategies, and multi-tenancy rules that govern the ProjectMind AI data tier. Operating strictly as a read-only integration layer above the organization's tool suites, the database is optimized to store configuration settings, connector logs, and semantic vector graphs.

By outlining the logical schema in a SQL-independent and code-independent format, this document serves as the guide for engineers writing Java JPA entities and configuring relational and vector databases, ensuring data isolation, query latency compliance, and auditability.

---

# Database Overview

The platform data tier employs the following database design choices:

* **Database Technology:** PostgreSQL. It serves as the single transactional database, combining relational metadata schemas with vector storage extensions.
* **Why PostgreSQL:** PostgreSQL is chosen for its compliance, transactional robustness, query performance, and active enterprise support. It eliminates the operational overhead of running separate relational and document databases.
* **Why pgvector:** The pgvector extension adds native vector similarity search support to PostgreSQL. It allows the platform to store semantic text embedding arrays and run vector similarity search queries using standard indexing strategies, simplifying data architectures.
* **Multi-Tenant Strategy:** ProjectMind AI enforces logical tenant isolation. A mandatory organization identifier (tenant ID) partitions all relational configuration logs and vector records, ensuring that database queries dynamically filter out other tenants' data.
* **Data Ownership:** Each microservice owns its respective logical data models. Cross-service data requests are resolved using signed tokens at the API gateway layer rather than direct database joins, preserving microservice autonomy.

---

# Entity Inventory

The logical database entities required for the ProjectMind AI platform are mapped below:

| Entity Name | Purpose | Owner Microservice |
|---|---|---|
| **Organization** | Represents a corporate tenant and sets domain rules. | Organization Service |
| **User** | Represents a registered platform user account. | Authentication Service |
| **Role** | Defines a list of predefined user permissions (Engineer, Lead, Admin). | Authentication Service |
| **OrganizationMember** | Associates a user profile with an organization and role. | Organization Service |
| **Project** | Workspace grouping codebase connectors and documentation. | Project Service |
| **KnowledgeSource** | Connector connection credentials (GitHub token, Jira URL). | Knowledge Service |
| **Repository** | Details of connected code repositories synced from GitHub. | Knowledge Service |
| **Document** | Meta-data pointing to an ingested codebase file or wiki page. | AI Service |
| **KnowledgeChunk** | Split text segment extracted from an ingested document. | AI Service |
| **Embedding** | Mathematical vector representation of a knowledge chunk. | AI Service |
| **AIConversation** | Session thread created by a developer. | Search Service |
| **AIMessage** | Individual question or grounded answer inside a thread. | Search Service |
| **AuditLog** | Immutable security audit logs of queries and changes. | Administration Service |

---

# Entity Relationships

The logical dependencies between entities are structured as follows:

* **Organization → Users (via OrganizationMember):** One-to-many. An Organization can have many OrganizationMembers (which reference unique Users).
* **Organization → Projects:** One-to-many. An Organization hosts multiple project workspaces.
* **Project → Knowledge Sources:** One-to-many. A Project workspace links to multiple tool connectors (GitHub, Jira, Confluence).
* **Knowledge Source → Documents:** One-to-many. A connected source yields multiple text file records or ticket logs.
* **Document → Knowledge Chunks:** One-to-many. A codebase file or wiki document is split into multiple tokenized text segments.
* **Knowledge Chunk → Embedding:** One-to-one. Every text segment maps to exactly one vector embedding token.
* **User → AI Conversations:** One-to-many. A developer profile creates multiple Q&A history threads.
* **AI Conversation → AI Messages:** One-to-many. A conversation thread contains a chronological log of Q&A messages.

---

# Primary Keys

The platform database enforces a unified primary key strategy:
* **UUID Key Strategy:** All primary key fields (`id`) must utilize Universally Unique Identifiers (UUID v4) instead of sequential integers.
* **Rationale:** UUIDs protect against ID scanning vulnerabilities, decouple key generation from database locks, and simplify database migrations and replication.

---

# Foreign Keys

The foreign key dependencies connecting the logical entities are mapped below:

| Source Entity | Foreign Key Field | Target Parent Entity | Cascading Behavior |
|---|---|---|---|
| **OrganizationMember** | `organization_id` | **Organization** | RESTRICT |
| **OrganizationMember** | `user_id` | **User** | RESTRICT |
| **Project** | `organization_id` | **Organization** | RESTRICT |
| **KnowledgeSource** | `project_id` | **Project** | RESTRICT |
| **Repository** | `knowledge_source_id` | **KnowledgeSource** | CASCADE |
| **Document** | `knowledge_source_id` | **KnowledgeSource** | CASCADE |
| **KnowledgeChunk** | `document_id` | **Document** | CASCADE |
| **Embedding** | `knowledge_chunk_id` | **KnowledgeChunk** | CASCADE |
| **AIConversation** | `user_id` | **User** | RESTRICT |
| **AIMessage** | `conversation_id` | **AIConversation** | CASCADE |
| **AuditLog** | `user_id` | **User** | RESTRICT |

---

# Constraints

ProjectMind AI enforces logical constraints to maintain data integrity:

* **NOT NULL Constraints:** All identifier fields, emails, passwords, primary keys, and organization identifiers must be non-nullable.
* **UNIQUE Constraints:**
  * `User.email` must be unique globally.
  * `Organization.domain_suffix` must be unique globally.
  * `Project.namespace` must be unique within the tenant organization boundary.
  * `Repository.url` must be unique within the target project.
* **CHECK Constraints:**
  * `User.status` must only contain values in `('Active', 'Inactive', 'Suspended')`.
  * `KnowledgeSource.type` must only contain values in `('GitHub', 'Jira', 'Confluence', 'Manual')`.
  * `AIMessage.role` must only contain values in `('Developer', 'System', 'AI')`.
  * `Embedding.vector_dimension` must equal the specific embedding model output dimension (e.g. 1536).
* **Foreign Key Constraints:** Referential integrity is protected via ON DELETE RESTRICT on core entities (Organizations, Users, Projects) to prevent cascading data loss.

---

# Indexing Strategy

To guarantee P95 latencies under 2.0 seconds, the database implements target indexing:

* **Authentication Lookups:** Unique B-Tree indexes on `User.email` and `Organization.domain_suffix` to support fast login.
* **Organization & Project Filtering:** Composite B-Tree indexes on foreign keys (`organization_id`, `project_id`) on project and connector tables to accelerate workspace routing.
* **Audit & Activity Feeds:** Descending B-Tree indexes on `created_at` fields on logs and messages tables to speed up timeline rendering.
* **AI Semantic Search:** **HNSW (Hierarchical Navigable Small World)** indexing on the `Embedding.vector` column. HNSW provides faster search speeds compared to IVF, satisfying strict IDE query latency limits.

---

# Soft Delete Strategy

Data deletion follows a logical soft-delete pattern:
* **Soft Delete Flag:** Every auditable database record features a boolean flag field (`deleted`, defaulting to `false`).
* **Deactivation Process:** Delete commands perform an UPDATE query setting the `deleted` flag to `true`, rather than hard-deleting the row.
* **Query Isolation:** Relational select queries dynamically append `WHERE deleted = false` to filter out archived data.
* **Compliance Archival:** Hard deletes are executed only by authorized background cron scripts during database cleanups for compliance (e.g., GDPR right to be forgotten).

---

# Audit Fields

Every logical database entity includes standard auditing fields to track data updates:

* `id`: Unique UUID v4 primary identifier.
* `created_at`: Date-time timestamp logging record creation.
* `updated_at`: Date-time timestamp logging the last modification.
* `created_by`: User UUID identifying the creator.
* `updated_by`: User UUID identifying the updater.
* `deleted`: Boolean flag indicating logical deletion status.
* `version`: Long integer number supporting optimistic locking version control.

---

# Multi-Tenancy Strategy

Logical partitioning ensures data privacy across corporate accounts:
* **Tenant Isolation Key:** Every project, document, chat thread, and connector record includes an `organization_id` foreign key.
* **Query Boundary Filtering:** Service repositories automatically append the active tenant's `organization_id` (obtained from the user's JWT) to all database query parameters.
* **Database Isolation:** Database access roles restrict service connections to their corresponding schema boundaries, preventing cross-tenant access.

---

# Data Retention Strategy

The platform maintains data lifecycle policies:
* **Workspace Settings:** Retained indefinitely until the project or organization is deactivated.
* **Sync Log delta Queues:** Retained for 30 days to troubleshoot connection errors, then purged.
* **Audit logs:** Retained for a minimum of 365 days in an immutable state for compliance.
* **Archived Projects:** Retained in a soft-deleted state for 90 days before background tasks execute permanent deletion of vector indexes.

---

# Risks

Operating the database schema faces key transactional and security risks:

### Cross-Tenant Data Contamination
* *Risk:* Bugs in query logic allow developers to search across other organizations' index databases.
* *Mitigation:* Enforce tenant ID parameters validation at the repository query gate, checking organization boundaries on every search.

### Vector Search Performance Lag
* *Risk:* Large repository indexing causes HNSW vector queries to slow down.
* *Mitigation:* Partition vector index databases by project workspace IDs, isolating query execution scopes.

### Out of Memory database locks
* *Risk:* Concurrent bulk commits from ingestion pipelines lock PostgreSQL tables, blocking client search actions.
* *Mitigation:* Run write transactions asynchronously using delta queue staging files.

---

# Conclusion

The ProjectMind AI Database Schema defines the logical data boundaries, entity mappings, constraints, and vector index configurations that support the platform. By utilizing UUID identifiers, logical tenant filters, soft-delete audits, and pgvector HNSW indexing, this schema supports pilot deployments while preparing the development team for secure database implementation.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Database Schema Document. |
