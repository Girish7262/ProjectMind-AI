# Low Level Design

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | System Design / Low Level Design |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

# Executive Summary

The purpose of this Low Level Design (LLD) document is to detail the internal design and processing behaviors of each major module in the ProjectMind AI platform. This document translates the high-level architectures from [HIGH_LEVEL_DESIGN.md](file:///e:/projectmind/docs/06-system-design/HIGH_LEVEL_DESIGN.md) into concrete functional guides for developers, specifying inputs, outputs, validation boundaries, internal sub-components, dependencies, and exception handling paths.

By establishing these designs in an implementation-independent manner, this document provides the the developer with a roadmap for writing code, building API schemas, configuring databases, and executing automated QA test suites.

---

# Module Design

ProjectMind AI divides its functionality into eight core modules:

## Module 1 – Authentication & Authorization
* **Purpose:** Govern secure user access and role mappings.
* **Responsibilities:** Validate credentials, authenticate users via SAML SSO, issue secure JWT session signatures, and filter search queries based on user roles.
* **Inputs:** Login emails, passwords, SAML identity provider assertions, active JWT authorization headers.
* **Outputs:** Secure JSON Web Tokens (JWT), session authorization states, role mapping objects, invalid login errors.
* **Validation Rules:** Enforce email formatting constraints, password complexity rules, and verification of non-expired JWT tokens.
* **Internal Components:**
  * **SSO Connector:** Handles SAML certificate validation and callback configurations with Okta/corporate directories.
  * **Token Generator:** Creates and signs secure JWT session tokens with predefined expiry.
  * **Role Mapper:** Maps synchronized identity group fields to local platform roles.
  * **Session Validator:** Intercepts API requests to verify authorization header validity.
* **Dependencies:** Database (for credentials hashes and roles configurations), Organization Management.
* **Error Handling:** Returns *401 Unauthorized* for credentials failures; returns *403 Forbidden* for expired tokens or unauthorized role actions; locks accounts after 5 consecutive failures.

---

## Module 2 – Organization Management
* **Purpose:** Establish isolated tenant scopes and manage tenant users.
* **Responsibilities:** Initialize isolated tenant databases, process team invitations, and edit organizational metadata settings.
* **Inputs:** Organization name, domain prefix parameters, email invite lists, SSO endpoints.
* **Outputs:** Tenant spaces, email invitation links, active membership profiles.
* **Validation Rules:** Enforce corporate domain suffix requirements; verify uniqueness of tenant namespace prefixes.
* **Internal Components:**
  * **Tenant Initializer:** Allocates and provisions isolated database schema structures for the tenant.
  * **Invitation Manager:** Generates secure token invitation links and routes emails.
  * **Role Configuration Manager:** Governs user roles and membership status boundaries.
* **Dependencies:** Database, Authentication & Authorization.
* **Error Handling:** Returns validation warnings on duplicate domain registration; invalidates expired user invitation tokens.

---

## Module 3 – Project Management
* **Purpose:** Define logical project workspaces to group codebase files, requirements, and wikis.
* **Responsibilities:** Create, edit, and archive project workspaces.
* **Inputs:** Project workspace name, workspace namespace, update settings, archive commands.
* **Outputs:** Project configurations, initialized workspaces, updated metadata, archived project states.
* **Validation Rules:** Project names must be unique within the tenant boundary; namespaces must follow directory formatting syntax.
* **Internal Components:**
  * **Project Lifecycle Controller:** Directs states (Active, Archived) and configuration updates.
  * **Workspace Creator:** Initializes project records and creates vector database partitions.
  * **Archive Handler:** Halts connector updates and flags index data as inactive.
* **Dependencies:** Database, Organization Management.
* **Error Handling:** Throws exceptions if the target project identifier does not exist; blocks duplicate project registrations.

---

## Module 4 – Knowledge Source Management
* **Purpose:** Manage integrations and synchronize codebase updates, requirement logs, and wikis.
* **Responsibilities:** Ingest source code repositories, tickets, and wiki pages using read-only API connectors.
* **Inputs:** Git repository URLs, Jira project keys, Confluence space IDs, read-only API tokens, local Markdown document uploads, Git push webhooks.
* **Outputs:** Ingested source files, ticket logs, wikis, synchronization queues, sync status reports.
* **Validation Rules:** Repository URLs must follow valid syntax formats; manual document uploads must be text-parseable and less than 10MB in size.
* **Internal Components:**
  * **GitHub Connector:** Queries GitHub REST/GraphQL APIs to fetch codebase structures, commit histories, and review comments.
  * **Jira Connector:** Queries Jira APIs to download user story text, ticket logs, and assignee metadata.
  * **Confluence Connector:** Queries Confluence APIs to fetch space folders and runbook wikis.
  * **Document Upload Handler:** Validates and processes manual Markdown uploads.
  * **Synchronization Process Queue:** Manages delta processing, orchestrating incremental updates from commits and webhooks.
* **Dependencies:** Project Service, Database, External Systems (GitHub, Jira, Confluence APIs).
* **Error Handling:** Implements exponential back-offs when hitting API rate limits; alerts admins on connection credential failures (*401 Unauthorized*).

---

## Module 5 – AI Knowledge Engine
* **Purpose:** Parse ingested engineering resources and build semantic relationship indexes.
* **Responsibilities:** Run syntax tree parsing, generate vector embedding tokens, map requirement-to-code dependencies, and generate grounded answers.
* **Inputs:** Codebase files, commit histories, Jira tickets, Confluence wikis, user queries.
* **Outputs:** Semantic vector indexes, context-aware answers, clickable codebase and ticket citations.
* **Validation Rules:** Code files must follow standard syntax formatting; generated answers must map directly to retrieved context files.
* **Internal Components:**
  * **Knowledge Indexing Parser:** Asynchronously parses code structures, commit messages, tickets, and wikis.
  * **Embedding Generator:** Encodes text segments into vector spaces using semantic embeddings models.
  * **Retrieval Process Matcher:** Matches queries against vector spaces, returning matching context snippets.
  * **AI Question Processing Engine:** Coordinates vector lookup, applies prompt formatting, and enforces safety filters.
  * **Response Generation Writer:** Drafts structured, natural language responses based on retrieved context files.
  * **Source Referencing CITer:** Extracts file paths (`file:///...`), line numbers, and Jira IDs, attaching them to response segments.
* **Dependencies:** Database, Knowledge Source Management.
* **Error Handling:** Skips corrupt or unparseable files; returns "context not found" if semantic retrieval confidence falls below target thresholds, preventing hallucinations.

---

## Module 6 – Search
* **Purpose:** Process and filter user conceptual searches.
* **Responsibilities:** Accept search queries, apply user role boundaries (RBAC filters), rank matching indexes, and format search outputs.
* **Inputs:** User conceptual queries, user JWT session keys, target workspace boundaries.
* **Outputs:** Ranked semantic file lists, ticket matches, search log entries.
* **Validation Rules:** Enforce query safety rules (prevent injection scripts); verify user authorization before executing matches.
* **Internal Components:**
  * **Search Processing Controller:** Receives query variables, parses intent, and calls vector matchers.
  * **Filtering Logic:** Applies real-time RBAC filters matching user permissions on GitHub/Jira.
  * **Ranking Algorithm:** Sorts search returns based on conceptual confidence scores.
  * **Result Presentation Formatter:** Structures search results into files list and citation tags.
* **Dependencies:** AI Knowledge Engine, Authentication & Authorization.
* **Error Handling:** Rejects queries if the session JWT is invalid or missing; returns empty lists if user is unauthorized.

---

## Module 7 – Dashboard
* **Purpose:** Report indexing status, timeline feeds, and platform performance.
* **Responsibilities:** Chart query volumes, display active connection statuses, and report sync activity events.
* **Inputs:** Index size logs, connector status parameters, sync timestamps, search latencies.
* **Outputs:** Index health graphs, latency charts, recent activity timelines, active user counts.
* **Validation Rules:** Enforce date-range boundaries on metrics charts; validate format compliance of telemetry data.
* **Internal Components:**
  * **Dashboard Components Compiler:** Organizes data for the web console dashboards.
  * **Statistics Engine:** Computes search latencies, query counts, and helper ratings.
  * **Activity Feed Aggregator:** Polls recent commit syncs and ticket updates.
  * **Health Indicators Logger:** Monitors connection health (Connected, Syncing, Offline).
* **Dependencies:** Project Management, Knowledge Source Management.
* **Error Handling:** Displays default baseline graphs if telemetry logs are temporarily unavailable.

---

## Module 8 – Administration
* **Purpose:** Govern global platform settings and user roles.
* **Responsibilities:** Audit configuration changes, export system audit logs, toggle user profile status, and set rate-limiting policies.
* **Inputs:** Account deactivation triggers, system audit files, API rate configuration inputs.
* **Outputs:** Relational audit logs sheets, updated credentials tokens, modified user profiles.
* **Validation Rules:** Admins cannot deactivate their own active profile; configuration parameters must fall within safe bounds.
* **Internal Components:**
  * **User Management Controller:** Handles user statuses and profiles activation.
  * **Role Management Console:** Modifies user privileges (Engineer, Admin).
  * **Audit Logs Writer:** Records immutable logs of search queries, logins, and configurations.
  * **System Settings Handler:** Manages API rate bounds and indexing intervals.
* **Dependencies:** Authentication Service, Organization Management.
* **Error Handling:** Returns validation warnings on self-deactivation; falls back to default rates if system configuration parameters are corrupt.

---

# Module Dependency Matrix

The following matrix maps the dependencies between internal platform modules:

| Module Name | Depends On | Used By |
|---|---|---|
| **Module 1: Authentication & Authorization** | Database | Module 2, Module 3, Module 6, Module 8 |
| **Module 2: Organization Management** | Database, Module 1 | Module 3, Module 8 |
| **Module 3: Project Management** | Database, Module 2 | Module 4, Module 7 |
| **Module 4: Knowledge Source Management** | Database, Module 3 | Module 5, Module 7 |
| **Module 5: AI Knowledge Engine** | Database, Module 4 | Module 6 |
| **Module 6: Search** | Module 1, Module 5 | Angular Frontend, IDE Plugins |
| **Module 7: Dashboard** | Module 3, Module 4 | Angular Frontend |
| **Module 8: Administration** | Module 1, Module 2 | Organization Admins |

---

# Internal Communication Flow

ProjectMind AI employs two primary internal communication patterns:

* **Synchronous REST Communication:** Client queries (such as search lookups or logins) are routed via the API Gateway using secure HTTPS/JSON structures to guarantee low-latency round trips.
* **Asynchronous Event Communication:** Ingestion pipelines and indexing jobs are orchestrated via database queues and webhook events, allowing long-running tasks (such as cloning repository updates or parsing files) to run in the background without locking developer search APIs.

---

# Validation Strategy

To prevent database contamination and security leaks, the platform enforces validation rules at two system boundaries:

* **API Gateway Gate:** Validates JWT signatures, parses query parameters for safety, blocks empty values, and filters invite domains.
* **Service Database Gate:** Enforces relational constraints (such as unique namespaces and domain registrations), ensures files are text-parseable, and enforces size boundaries (<10MB uploads).

---

# Error Handling Strategy

Errors are categorized and handled according to specific operational policies:

* **Validation Errors:** Returns validation errors to the client, flagging missing fields or incorrect email/URL formats.
* **Authentication Errors:** Returns *401 Unauthorized*, blocking session setup and incrementing password fail logs.
* **Authorization Errors:** Returns *403 Forbidden*, filtering out search results that violate the user's synced repository access rights.
* **Integration Errors:** Employs exponential retry logic for GitHub/Jira timeouts, flagging connection statuses as "Offline" after three failures.
* **AI Processing Errors:** Skips parse errors on corrupt files, logging warnings; returns "context not found" if vector confidence is below target thresholds.
* **System Errors:** Returns *500 Internal Server Error* and registers trace histories in central logs, while returning safe messages to the frontend.

---

# Logging Strategy

ProjectMind AI maintains four isolated log structures:

* **Application Logs:** Captures debug and info traces, tracking microservices health and queue processing times.
* **Audit Logs:** Records immutable logs of user configurations changes, login events, and credential updates for compliance.
* **Error Logs:** Captures all exception traces, gateway failures, and connector timeouts for troubleshooting.
* **Security Logs:** Records login failures, role updates, and unauthorized access attempts (*403 Forbidden*) for security audits.

---

# Design Assumptions

The low-level design is based on the following architectural assumptions:
* Third-party systems expose stable APIs to retrieve code files, commits, tickets, and wiki pages.
* Source systems support OAuth or API token authentication mechanisms.
* Code comments and wikis are stored in text-parseable formats.
* Enterprise customers use centralized identity systems (SAML/Okta) for user authentication.

---

# Design Constraints

The system design operates within the following constraints:
* **Read-Only Ingestion Constraints:** The platform must execute using read-only API credentials, with no code mutation capabilities.
* **IDE Latency Bounds:** P95 search query response latencies must remain below 2.0 seconds.
* **Connector Rate Pacing:** Ingestion queries must limit throughput to prevent rate-limit throttling by GitHub/Jira APIs.
* **Supported Formats:** Parsing is restricted to text-parseable codebase files and documentation formats.

---

# Risks

The execution of these modules faces key operational and development risks:

### API Rate-Limit Throttling
* *Risk:* Ingestion pipelines block or stall during initial syncs of large repositories due to API rate limits.
* *Mitigation:* Employ incremental indexing, parsing only codebase deltas from webhook commit triggers.

### Unauthorized Internal Data Leaks
* *Risk:* Developers retrieve search results containing confidential source files or tickets they are not authorized to view in source systems.
* *Mitigation:* Enforce RBAC filtering during query processing, matching search parameters with the user's active GitHub/Jira access token groups.

### Grounding Hallucinations
* *Risk:* The AI engine generates false technical advice.
* *Mitigation:* Ground outputs strictly in retrieved files, returning "context not found" if semantic confidence is low.

---

# Conclusion

This Low Level Design provides the detailed design of each major module in ProjectMind AI, defining component behaviors, validations, dependencies, and exception handling paths. By establishing these module-level guidelines, the LLD enables developers to implement functional services, API controllers, and database partitions that are secure, isolated, and scalable.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Low Level Design Document. |
