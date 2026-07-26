# Business Rules

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Business Requirements / Business Rules |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

## Executive Summary

Enterprise software environments require clear constraints to safeguard intellectual property, preserve security boundaries, and ensure compliance. Business rules define the operational boundaries and validations that govern the behavior of the ProjectMind AI platform.

These rules translate corporate security policies, role permissions, and compliance goals into concrete product constraints. This document outlines the business rules governing user management, organizations, knowledge management, AI behavior, security, compliance, and input validation, mapping rule dependencies and the risks of violations.

---

## User Management Rules

* **UM-001: Mandatory Authentication:** Only successfully authenticated users with valid active session tokens can access the ProjectMind AI web workspace or IDE plugin.
* **UM-002: Single-Organization Affiliation:** Every user account must belong to exactly one tenant organization.
* **UM-003: Predefined Role Assignment:** Every user must be assigned to a predefined system role (e.g., Engineer, Tech Lead, Administrator) that determines their platform boundaries.
* **UM-004: Role-Based Authorization Enforcement:** All platform operations and search actions must dynamically check the user's role permissions before processing.

---

## Organization Rules

* **OR-001: Single-Organization Project Ownership:** Every registered codebase repository, Jira board, or Confluence wiki connected to the platform must belong to a single organization tenant.
* **OR-002: Administrative Self-Service:** Organization administrators manage user invitations, role changes, and API connection credentials within their tenant boundary.
* **OR-003: Tenant Knowledge Isolation:** Ingested source code files, git logs, tickets, and wiki databases must be completely isolated between tenant organizations.
* **OR-004: Prohibition of Cross-Tenant Access:** Under no circumstances can a user query, search, or view metadata belonging to a different organization.

---

## Knowledge Management Rules

* **KM-001: Approved Source Ingestion:** Only codebase repositories, Jira trackers, and Confluence spaces explicitly registered and approved by organization administrators can be indexed.
* **KM-002: Source Data Traceability:** The platform must preserve the lifecycle origin of all indexed files, commits, tickets, and wiki edits.
* **KM-003: Grounded Ingestion Citations:** Ingested content stored in the semantic database must retain pointers to its original system source (e.g., file paths, issue IDs, wiki URLs).
* **KM-004: Context Synchronization Cycle:** The semantic index must update incrementally when commit webhooks fire, and re-index directories periodically to detect stale context.

---

## AI Interaction Rules

* **AI-001: Gated Knowledge Boundary:** AI-generated answers must be formulated using only the authorized codebase files, requirements, and wikis connected to the user's active workspace.
* **AI-002: Mandatory Grounded Citations:** Every generated response must include explicit links citing the file paths, line ranges, and ticket logs used to formulate the answer.
* **AI-003: Grounding-Only Retrieval:** The platform must decline to answer a user query if the connected workspace indexes contain insufficient context, avoiding hallucinations.
* **AI-004: Permission-Aware Query Filters:** User queries must dynamically filter and exclude search results from directories or ticket projects the user does not have permission to access on the source systems.

---

## Security Rules

* **SEC-001: Source Permission Alignment (RBAC):** Access to ingested codebase files and requirements must align with the role-based access controls (RBAC) synchronized from GitHub and Jira.
* **SEC-002: Encryption Standards:** All project data retrieved during ingestion from target tools must be encrypted during transit and at rest.
* **SEC-003: Session Expiry Governance:** User session tokens must automatically expire and require re-authentication after a predefined period of inactivity.
* **SEC-004: Activity Audit Logging:** The platform must log all user searches, login events, connector setting updates, and credential changes.

---

## Compliance Rules

* **COMP-001: Private Data Boundaries:** Customer codebase files, Git logs, and ticket texts must never be transmitted to public generative AI models for training or parsing.
* **COMP-002: Access Auditability:** Audit log history must be exportable by authorized compliance officers for corporate security reviews.
* **COMP-003: Credential Rotation:** Read-only OAuth tokens and API access keys used for tool ingestion must be secured and rotated periodically.

---

## Validation Rules

* **VAL-001: Mandatory Registration Fields:** Project registration requests must fail if mandatory identifiers (such as Project Name, Git Repository URL) are empty.
* **VAL-002: Duplicate Project Prevention:** The platform must reject project registrations sharing a namespace or repository URL within the same tenant.
* **VAL-003: Repository URL Validation:** Ingested repository paths must follow standard URL and directory formatting syntax.
* **VAL-004: Supported Formats Constraints:** The parsing engine must only process text-parseable codebase files and documentation formats (Markdown, rich text, HTML, JSON), ignoring unparseable binaries.

---

## Business Rule Matrix

The following matrix prioritizes the key business rules governing ProjectMind AI:

| Rule ID | Business Rule Description | Category | Priority |
|---|---|---|---|
| **UM-001** | Only authenticated users can access the platform. | User Management | Must Have |
| **OR-003** | Knowledge and indexes are isolated between organizations. | Organization | Must Have |
| **KM-003** | Ingested content must retain source system references. | Knowledge Management | Must Have |
| **AI-002** | Every AI response must include grounded source citations. | AI Interaction | Must Have |
| **AI-004** | Search queries must respect source platform access rights (RBAC). | AI Interaction | Must Have |
| **SEC-004** | Audit logs must capture all queries, logins, and settings changes. | Security | Must Have |
| **COMP-001** | Customer code assets must never be sent to public models. | Compliance | Must Have |
| **VAL-002** | Prevent registering duplicate repository URLs within a tenant. | Validation | Should Have |

---

## Rule Dependencies

ProjectMind AI's business rules exhibit key operational dependencies:

* **SEC-001 (RBAC Integration)** depends on **UM-003 (Role Assignment)** and **UM-001 (Mandatory Authentication)** to properly map query filter boundaries.
* **AI-001 (Gated Knowledge)** and **AI-002 (Mandatory Citations)** depend on **KM-003 (Grounded Ingestion Citations)** to link answers back to source code lines and ticket IDs.
* **OR-004 (Prohibition of Cross-Tenant Access)** depends on **OR-003 (Tenant Isolation)** and **UM-002 (Single-Organization Affiliation)** to restrict query parameters.

---

## Risks of Rule Violations

Violating these rules introduces operational, legal, and security risks:

### Unauthorized Internal Data Exposure
* *Risk:* Failure of **SEC-001 (RBAC)** allows developers to retrieve search results from restricted payroll or security directories.
* *Mitigation:* Perform real-time permission checks on the user's active GitHub/Jira access profile before compiling semantic search results.

### Customer IP Leakage
* *Risk:* Violating **COMP-001 (Private Data Boundaries)** exposes proprietary codebase logic to public training models.
* *Mitigation:* Deploy private open-source models inside the customer's secure VPC, blocking outbound public cloud connections.

### Inaccurate Grounding (Hallucinations)
* *Risk:* Violating **AI-003 (Grounding-Only Retrieval)** causes the model to guess answers when context is missing, introducing code bugs.
* *Mitigation:* Ground query answers strictly in retrieved source files, returning a clear "context not found" message if semantic confidence is low.

### Tenant Cross-Contamination
* *Risk:* Failure of **OR-003 (Tenant Isolation)** allows a user query to return index data belonging to another enterprise customer.
* *Mitigation:* Enforce logical database isolation matching tenant IDs on all query pipelines.

---

## Conclusion

Business rules ensure consistent, secure, and compliant platform behavior for ProjectMind AI. By establishing firm boundaries for user authentication, tenant isolation, semantic citations, and private model execution, these rules secure the enterprise's codebase investments while providing developers with a safe, unblocked workflow workspace.

---

## Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Business Rules Document. |
