# Use Cases

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Functional Requirements / UML Use Cases |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

## Executive Summary

Use case documentation details the system behaviors of ProjectMind AI from the perspective of external actors (developers, administrators, and integrated subsystems). Each use case describes a structured flow of events through which an actor interacts with the platform to achieve a defined operational goal.

By defining triggers, preconditions, main flow steps, alternative flows, exception gates, and postconditions, this document translates functional specifications into precise development paths. This ensures that coding and automated QA testing sequences align with the system boundaries established in [PRODUCT_SCOPE.md](file:///e:/projectmind/docs/02-product-vision/PRODUCT_SCOPE.md).

---

## Actors

ProjectMind AI recognizes the following actors:

* **Administrator:** Global platform operator responsible for billing setups, tenant creation, and infrastructure monitoring.
* **Organization Admin:** Tenant-level administrator responsible for inviting users, managing roles, and configuring API connector credentials.
* **Developer:** Core system user who runs natural language queries and codebase searches within their IDE workspace.
* **Tech Lead:** Technical user who reviews pull requests, analyzes system dependencies, and monitors codebase compliance.
* **Project Manager:** Product manager who validates logic mapping and traces codebase features back to Jira requirement tickets.
* **AI System:** Asynchronous background parser and semantic matching engine responsible for parsing codebase syntax, mapping relations, and generating grounded responses.

---

## Use Cases

The following sections define the core use cases for the ProjectMind AI platform:

### UC-001 User Registration
* **Actor:** Developer, Tech Lead, Project Manager
* **Goal:** Register a new user profile within a tenant organization.
* **Preconditions:** System is online; corporate domain and organization tenant are registered.
* **Trigger:** User clicks registration link on the login interface.
* **Main Flow:**
  1. User enters name, corporate email address, and password.
  2. System validates email domain matches tenant organization constraints.
  3. System saves user credentials and sends an activation email with a secure token.
  4. User clicks the activation link in the email.
  5. System activates user account and redirects user to login console.
* **Alternative Flow (SSO Registration):**
  1. User selects SSO option (e.g., Okta).
  2. System redirects user to corporate identity provider.
  3. User logs in successfully; identity provider returns user metadata.
  4. System automatically registers profile and redirects user to workspace.
* **Exception Flow:**
  1. *Domain Mismatch:* Email domain does not match any registered organization domain. System rejects registration and displays a verification error.
* **Postconditions:** User account is created and activated for query lookups.
* **Business Rules:** **UM-002** (Single-Organization Affiliation), **UM-003** (Role Assignment).
* **Related User Stories:** **US-001**
* **Priority:** Must Have

### UC-002 Login
* **Actor:** Developer, Tech Lead, Project Manager, Org Admin, Administrator
* **Goal:** Authenticate and establish a secure query session.
* **Preconditions:** User profile is registered and activated.
* **Trigger:** User opens the web console or initializes the IDE extension.
* **Main Flow:**
  1. User inputs email and password.
  2. System validates credentials against secure password hash.
  3. System generates JSON Web Token (JWT) session signature.
  4. System redirects user to active dashboard workspace.
* **Alternative Flow (SAML SSO Login):**
  1. User selects corporate SSO button.
  2. System validates SAML tokens returned from Okta/identity directory.
  3. System loads active user session tokens.
* **Exception Flow:**
  1. *Invalid Credentials:* Validation fails. System blocks login and increments password failure count.
* **Postconditions:** Secure JWT session token is created for IDE and web actions.
* **Business Rules:** **UM-001** (Mandatory Authentication).
* **Related User Stories:** **US-002**
* **Priority:** Must Have

### UC-003 Create Organization
* **Actor:** Administrator
* **Goal:** Initialize an isolated corporate tenant organization.
* **Preconditions:** Global administrator is logged in.
* **Trigger:** Admin clicks "Initialize Tenant Organization" in the console.
* **Main Flow:**
  1. Admin enters tenant name, corporate email domain, and resource limits.
  2. System allocates isolated data partitions and creates organization workspace.
  3. System prompts creation of initial Organization Admin profile.
* **Exception Flow:**
  1. *Duplicate Domain:* Corporate domain is already associated with another tenant. System rejects creation.
* **Postconditions:** Isolated tenant organization is created and ready for users.
* **Business Rules:** **OR-003** (Tenant Knowledge Isolation), **OR-004** (Prohibition of Cross-Tenant Access).
* **Related User Stories:** **US-004**
* **Priority:** Must Have

### UC-004 Invite Team Members
* **Actor:** Organization Admin
* **Goal:** Send platform join invitations to the developer members.
* **Preconditions:** Org Admin is logged in and authenticated.
* **Trigger:** Admin opens "Invite Users" panel in settings.
* **Main Flow:**
  1. Admin inputs candidate email addresses and assigns initial system roles.
  2. System validates email formatting.
  3. System generates secure token invitation link and emails candidates.
* **Exception Flow:**
  1. *Invalid Email format:* System flags incorrect rows and blocks execution.
* **Postconditions:** Registration invitation email sent.
* **Business Rules:** **OR-002** (Admin user management).
* **Related User Stories:** **US-005**
* **Priority:** Must Have

### UC-005 Create Project
* **Actor:** Organization Admin
* **Goal:** Create a logical project workspace within the organization tenant.
* **Preconditions:** Admin is logged in.
* **Trigger:** Admin selects "Create New Project Workspace" in console.
* **Main Flow:**
  1. Admin enters project name, namespace, and description.
  2. System validates project namespace uniqueness within the tenant.
  3. System initializes workspace boundaries and logs configuration.
* **Exception Flow:**
  1. *Duplicate Namespace:* Namespace already exists. System requests new project name.
* **Postconditions:** Empty project workspace created and ready for tool connection.
* **Business Rules:** **OR-001** (Single-Organization Project Ownership).
* **Related User Stories:** **US-007**
* **Priority:** Must Have

### UC-006 Connect GitHub Repository
* **Actor:** Organization Admin
* **Goal:** Link a git codebase repository as a read-only knowledge source.
* **Preconditions:** Project workspace exists; admin has API access scopes.
* **Trigger:** Admin selects GitHub connector in project settings.
* **Main Flow:**
  1. Admin enters OAuth credentials or secure API token and inputs repository URL.
  2. System queries GitHub API to validate repository access parameters.
  3. System registers repository as active knowledge source and triggers initial ingestion query.
* **Exception Flow:**
  1. *Connection Failure:* GitHub API returns 401 Unauthorized or network timeouts. System flags connector as offline.
* **Postconditions:** GitHub repository linked as read-only knowledge source.
* **Business Rules:** **KM-001** (Approved Source Ingestion).
* **Related User Stories:** **US-011**
* **Priority:** Must Have

### UC-007 Connect Jira
* **Actor:** Organization Admin
* **Goal:** Link a Jira project board as a read-only knowledge source.
* **Preconditions:** Project workspace exists; admin has Jira API token.
* **Trigger:** Admin selects Jira connector in project settings.
* **Main Flow:**
  1. Admin enters Jira instance URL, user email, API token, and project key.
  2. System queries Jira API to validate credential access scopes.
  3. System registers Jira project and schedules initial ingestion.
* **Exception Flow:**
  1. *Authorization Block:* Jira API rejects access credentials. System alerts admin.
* **Postconditions:** Jira project linked as read-only knowledge source.
* **Business Rules:** **KM-001** (Approved Source Ingestion).
* **Related User Stories:** **US-012**
* **Priority:** Must Have

### UC-008 Connect Confluence
* **Actor:** Organization Admin
* **Goal:** Link a Confluence wiki space as a read-only knowledge source.
* **Preconditions:** Project workspace exists; admin has Confluence Space ID.
* **Trigger:** Admin selects Confluence connector in project settings.
* **Main Flow:**
  1. Admin enters Confluence URL, Space ID, and access tokens.
  2. System queries Confluence API to validate space metadata access.
  3. System registers space and schedules ingestion.
* **Exception Flow:**
  1. *Invalid Space ID:* Confluence space does not exist. System returns error.
* **Postconditions:** Confluence space linked as read-only knowledge source.
* **Business Rules:** **KM-001** (Approved Source Ingestion).
* **Related User Stories:** **US-013**
* **Priority:** Must Have

### UC-009 Upload Documents
* **Actor:** Developer, Tech Lead, Org Admin
* **Goal:** Upload supplementary local reference text files to the project index.
* **Preconditions:** User is logged in.
* **Trigger:** User drags reference document into web portal upload zone.
* **Main Flow:**
  1. User selects target project workspace.
  2. User selects markdown or rich text file.
  3. System validates file format and size limits.
  4. System uploads file and queues it for parsing.
* **Exception Flow:**
  1. *Unsupported Format:* User uploads binary executable. System rejects file and displays error.
* **Postconditions:** Document uploaded and scheduled for semantic indexing.
* **Business Rules:** **VAL-004** (Supported Formats Constraints).
* **Related User Stories:** **US-014**
* **Priority:** Must Have

### UC-010 Synchronize Knowledge Sources
* **Actor:** AI System
* **Goal:** Detect codebase delta modifications and queue indexing updates.
* **Preconditions:** Connectors are registered; webhook endpoints are configured.
* **Trigger:** GitHub push webhook fires, or scheduled sync cron triggers.
* **Main Flow:**
  1. AI System receives repository file list delta.
  2. System downloads code commits and ticket modifications.
  3. System queues updates in the database parsing pipeline.
* **Exception Flow:**
  1. *Source Offline:* Target APIs return gateway timeouts. AI System schedules retry in subsequent cycle.
* **Postconditions:** Code and ticket modifications queued in sync database.
* **Business Rules:** **KM-004** (Context Synchronization Cycle).
* **Related User Stories:** **US-015**
* **Priority:** Must Have

### UC-011 AI Knowledge Indexing
* **Actor:** AI System
* **Goal:** Parse codebase modifications and build conceptual semantic graph links.
* **Preconditions:** Sync delta queues contain un-parsed datasets.
* **Trigger:** Synchronization pipeline completes data download.
* **Main Flow:**
  1. System parses codebase syntax structure and logic maps.
  2. System extracts git logs, Jira ticket requirements, and wiki texts.
  3. System runs vector embedding generation.
  4. System updates the project-specific semantic knowledge graph.
* **Postconditions:** Workspace search database is fully updated and healthy.
* **Business Rules:** **KM-002** (Source Data Traceability), **KM-003** (Grounded Ingestion Citations).
* **Related User Stories:** **US-015**
* **Priority:** Must Have

### UC-012 Ask AI Question
* **Actor:** Developer, Tech Lead, Project Manager
* **Goal:** Resolve a natural language technical query.
* **Preconditions:** User is logged in; project workspace semantic index exists.
* **Trigger:** User types a question in the IDE search console.
* **Main Flow:**
  1. User submits question (e.g., *"Why does this payment retry logic exist?"*).
  2. System retrieves relevant code, commit, and requirements context from the semantic index.
  3. System checks user access rights against retrieved files to ensure RBAC compliance.
  4. System generates structured explanation and displays citations (filenames, tickets, lines).
* **Exception Flow:**
  1. *Sparse Context:* Vector search finds insufficient grounding data. System returns "context not found," preventing hallucination.
* **Postconditions:** Grounded answer returned with clickable citations.
* **Business Rules:** **AI-001** (Gated Knowledge), **AI-002** (Mandatory Citations), **AI-003** (Grounding-Only Retrieval).
* **Related User Stories:** **US-016**, **US-018**
* **Priority:** Must Have

### UC-013 Semantic Search
* **Actor:** Developer, Tech Lead, Project Manager
* **Goal:** Locate code modules using conceptual search filters.
* **Preconditions:** User is logged in.
* **Trigger:** User searches for a conceptual term (e.g., *"billing timeout exceptions"*).
* **Main Flow:**
  1. System queries the vector search index for matches.
  2. System checks user access levels on source tools.
  3. System displays lists of files and wiki pages containing the concepts.
* **Exception Flow:**
  1. *Expired Token:* Session token is invalid. System prompts re-login.
* **Postconditions:** Context-aware files list is returned, filtered by user RBAC profiles.
* **Business Rules:** **SEC-001** (Source Permission Alignment).
* **Related User Stories:** **US-017**, **US-019**
* **Priority:** Must Have

### UC-014 View Dashboard
* **Actor:** Engineering Manager, Tech Lead, Org Admin
* **Goal:** Monitor project indexing coverage and search performance statistics.
* **Preconditions:** User is logged in.
* **Trigger:** User selects dashboard console.
* **Main Flow:**
  1. System compiles active connection logs and indexing health coverage scores.
  2. System processes query volume charts and search latencies.
  3. Dashboard renders visual graphs and active activity timelines.
* **Postconditions:** Metric graphs display active configuration metrics.
* **Business Rules:** **SEC-004** (Activity Audit Logging).
* **Related User Stories:** **US-010**, **US-020**, **US-021**
* **Priority:** Should Have

### UC-015 Manage Users
* **Actor:** Organization Admin
* **Goal:** Audit active user registrations and permissions.
* **Preconditions:** Org Admin is logged in.
* **Trigger:** Admin opens user management console.
* **Main Flow:**
  1. Admin views the list of active user profiles.
  2. Admin toggles account states (Activate, Deactivate).
  3. Admin modifies roles and saves changes.
* **Exception Flow:**
  1. *Self-Deletion:* Admin attempts to deactivate their own account. System blocks action.
* **Postconditions:** User status modified in organization database.
* **Business Rules:** **OR-002** (Admin user management).
* **Related User Stories:** **US-006**, **US-022**
* **Priority:** Should Have

---

## Use Case Traceability Matrix

The following matrix maps use cases to their related user stories and functional requirements:

| Use Case ID | Use Case Name | Related User Story | Related Functional Requirement |
|---|---|---|---|
| **UC-001** | User Registration | **US-001** | **FR-01.01** (User Registration) |
| **UC-002** | Login | **US-002** | **FR-01.01** (Login) |
| **UC-003** | Create Organization | **US-004** | **FR-02.01** (Create Organization) |
| **UC-004** | Invite Team Members | **US-005** | **FR-02.01** (Invite Users) |
| **UC-005** | Create Project | **US-007** | **FR-03.01** (Create Project) |
| **UC-006** | Connect GitHub Repository | **US-011** | **FR-04.01** (Connect GitHub) |
| **UC-007** | Connect Jira | **US-012** | **FR-04.01** (Connect Jira) |
| **UC-008** | Connect Confluence | **US-013** | **FR-04.01** (Connect Confluence) |
| **UC-009** | Upload Documents | **US-014** | **FR-04.01** (Upload Documents) |
| **UC-010** | Synchronize Knowledge Sources | **US-015** | **FR-04.02** (Sync Knowledge) |
| **UC-011** | AI Knowledge Indexing | **US-015** | **FR-05.01** (Index Project Knowledge) |
| **UC-012** | Ask AI Question | **US-016**, **US-018** | **FR-05.02** (AI Question & Answer) |
| **UC-013** | Semantic Search | **US-017**, **US-019** | **FR-05.02** (Semantic Search), **FR-06.01** |
| **UC-014** | View Dashboard | **US-010**, **US-020**, **US-021** | **FR-07.01** (Dashboard Summary) |
| **UC-015** | Manage Users | **US-006**, **US-022** | **FR-08.01** (User Management) |

---

## Assumptions

The defined use cases rely on the following business and functional assumptions:
* Target systems expose stable APIs to retrieve code files, commits, tickets, and wiki pages.
* Client administrators authorize OAuth connections to GitHub, Jira, and Confluence.
* User roles can be synchronized from target tools to verify permission access boundaries.
* Code comments and wikis are text-parseable formats.

---

## Risks

The implementation and execution of these use cases face key functional risks:

### API Connection Outages
* *Risk:* Outages on GitHub or Jira APIs block sync webhooks, causing semantic index decay.
* *Mitigation:* Employ automated retries and alert logs to notify administrators of connection changes.

### Internal Data Contamination
* *Risk:* Inadequate tenant isolation boundaries allow cross-tenant query execution.
* *Mitigation:* Enforce logical database partitioning matching tenant IDs on all indexing and vector database queries.

### Hallucinations during Q&A
* *Risk:* The AI engine generates false codebase configurations, causing developer bugs.
* *Mitigation:* Restrict answers strictly to the ingested project context. Return "context not found" if confidence is low.

---

## Conclusion

Satisfying these use cases ensures that ProjectMind AI delivers a secure and structured developer workspace. By establishing clear interaction steps, preconditions, alternative flows, and exception mitigations, this use case specification guides developers and testers, translating functional requirements into stable, validation-ready software components.

---

## Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Use Cases Document. |
