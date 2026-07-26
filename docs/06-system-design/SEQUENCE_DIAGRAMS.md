# Sequence Diagrams

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | System Design / UML Sequence Diagrams |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

# Executive Summary

UML sequence diagrams illustrate the runtime interactions and message flows between system actors, client interfaces, microservices, and external repositories. By modeling chronological transaction sequences, these diagrams guide developers through API coordination, trace service boundaries, and assist QA engineers in writing integration test scenarios.

The workflows covered in this document map all critical pathways defined in the platform's functional modules. They represent happy-path operations and error exception handling, establishing clear parameters for system boundary compliance.

---

# Actors

The sequence diagrams utilize the following actors and component systems:

* **User / Developer / Tech Lead / Organization Admin:** External human actors executing actions.
* **Frontend:** The web browser dashboard or IDE workspace extension.
* **Authentication Service:** Service validating credentials and session tokens.
* **Organization Service:** Service managing isolated tenant metadata and user roles.
* **Project Service:** Service organizing project workspaces and connector boundaries.
* **Knowledge Service:** Service executing external tool integrations and document syncs.
* **AI Service:** Service parsing codebase syntax trees, generating vector embeddings, and indexing context.
* **Search Service:** Service matching queries against vector databases and applying RBAC filters.
* **Database:** The local relational configuration databases and vector index stores.
* **GitHub / Jira / Confluence:** External platforms containing codebase assets, ticket trackers, and wikis.

---

# Sequence Diagram 1

## User Registration
* **Description:** A new user registers a platform account using their company email domain.
* **Preconditions:** The company email domain is registered by the organization admin.
* **Main Flow:**
  1. The user enters registration details on the signup form.
  2. The Frontend validates email format and forwards the request to the Authentication Service.
  3. The Authentication Service checks the database to verify the email domain matches active tenant domains.
  4. The Authentication Service registers the user profile as inactive in the Database and triggers an activation email.
  5. The user receives the email, clicks the secure token link, and validates their account.

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant Auth as Authentication Service
    participant DB as Database
    
    User->>Frontend: Enter signup details (email, password)
    Frontend->>Auth: POST /register (name, email, password)
    Auth->>DB: Check domain registration
    DB-->>Auth: Domain verified (active organization)
    Auth->>DB: Save user profile (Status: Inactive)
    Auth-->>Frontend: Signup success (Redirect to email check)
    Auth->>User: Send activation email with secure token
    User->>Auth: Click activation token URL
    Auth->>DB: Update user status (Status: Active)
    Auth-->>User: Account active (Redirect to Login)
```

* **Postconditions:** User profile is created in the database and active for logins.

---

# Sequence Diagram 2

## User Login
* **Description:** User authenticates and receives a secure session token.
* **Preconditions:** User account is active in the database.
* **Main Flow:**
  1. User enters login details in the Frontend or IDE console.
  2. Frontend posts credentials to the Authentication Service.
  3. The service validates credentials against the database, generates a JWT token, and returns it to the client.

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant Auth as Authentication Service
    participant DB as Database
    
    User->>Frontend: Input email and password
    Frontend->>Auth: POST /login (credentials)
    Auth->>DB: Query credentials hash
    DB-->>Auth: Hash match verified
    Auth->>Auth: Generate secure JWT (Session Token)
    Auth-->>Frontend: Return JWT (Expires: 24h)
    Frontend-->>User: Open dashboard (Session active)
```

---

# Sequence Diagram 3

## Create Organization
* **Description:** Global administrator initializes a new corporate tenant organization.
* **Preconditions:** Global administrator session is active.

```mermaid
sequenceDiagram
    actor Admin as Administrator
    participant Frontend
    participant Org as Organization Service
    participant DB as Database
    
    Admin->>Frontend: Enter tenant name and domain suffix
    Frontend->>Org: POST /organizations (name, domain)
    Org->>DB: Check domain uniqueness
    DB-->>Org: Domain available
    Org->>DB: Create tenant schema & admin user
    Org-->>Frontend: Organization created success
    Frontend-->>Admin: Display organization dashboard
```

---

# Sequence Diagram 4

## Invite Team Member
* **Description:** Organization admin invites team members to join the tenant.
* **Preconditions:** Admin is logged in.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Org as Organization Service
    participant DB as Database
    
    OrgAdmin->>Frontend: Enter emails list and role
    Frontend->>Org: POST /invites (emails, role)
    Org->>DB: Save invitation token record
    Org->>Org: Send email invitation link
    Org-->>Frontend: Invitations sent successfully
    Frontend-->>OrgAdmin: Update pending invitation logs
```

---

# Sequence Diagram 5

## Create Project
* **Description:** Admin initializes a project workspace to group codebase connectors.
* **Preconditions:** Admin is logged in.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Proj as Project Service
    participant DB as Database
    
    OrgAdmin->>Frontend: Enter project name and namespace
    Frontend->>Proj: POST /projects (name, namespace)
    Proj->>DB: Check namespace uniqueness within tenant
    DB-->>Proj: Namespace available
    Proj->>DB: Save project configuration records
    Proj-->>Frontend: Project created successfully
    Frontend-->>OrgAdmin: Redirect to connectors configuration
```

---

# Sequence Diagram 6

## Connect GitHub Repository
* **Description:** Link a codebase repository path.
* **Preconditions:** Project workspace exists.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Know as Knowledge Service
    participant GH as GitHub
    participant DB as Database
    
    OrgAdmin->>Frontend: Input repository URL and OAuth token
    Frontend->>Know: POST /connectors/github (repo_url, token)
    Know->>GH: Validate repository token access
    GH-->>Know: 200 OK (access authorized)
    Know->>DB: Save connector configuration records
    Know-->>Frontend: GitHub linked successfully
    Frontend-->>OrgAdmin: Display active sync status
```

---

# Sequence Diagram 7

## Connect Jira Project
* **Description:** Link a Jira tracker board.
* **Preconditions:** Project workspace exists.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Know as Knowledge Service
    participant Jira
    participant DB as Database
    
    OrgAdmin->>Frontend: Input Jira URL, API token, project key
    Frontend->>Know: POST /connectors/jira (url, token, key)
    Know->>Jira: Verify project API access
    Jira-->>Know: 200 OK (project authorized)
    Know->>DB: Save Jira connector settings
    Know-->>Frontend: Jira linked successfully
    Frontend-->>OrgAdmin: Display connection success
```

---

# Sequence Diagram 8

## Connect Confluence Space
* **Description:** Link Confluence documentation folder.
* **Preconditions:** Project workspace exists.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Know as Knowledge Service
    participant Conf as Confluence
    participant DB as Database
    
    OrgAdmin->>Frontend: Enter Space ID and credentials
    Frontend->>Know: POST /connectors/confluence (space_id, credentials)
    Know->>Conf: Validate space access
    Conf-->>Know: Space details returned
    Know->>DB: Save Confluence connector settings
    Know-->>Frontend: Confluence connected
    Frontend-->>OrgAdmin: Sync scheduled message
```

---

# Sequence Diagram 9

## Upload Documents
* **Description:** User uploads supplementary reference document files.
* **Preconditions:** User is logged in.

```mermaid
sequenceDiagram
    actor Dev as Developer
    participant Frontend
    participant Know as Knowledge Service
    participant DB as Database
    
    Dev->>Frontend: Drag document file (.md, size < 10MB)
    Frontend->>Know: POST /documents/upload (file, project_id)
    Know->>Know: Validate format & file size limits
    Know->>DB: Save file in project staging index
    Know-->>Frontend: Upload complete
    Frontend-->>Dev: Display document in workspace list
```

---

# Sequence Diagram 10

## Synchronize Knowledge Sources
* **Description:** GitHub push event webhooks trigger delta downloads.
* **Preconditions:** Push webhook endpoints configured.

```mermaid
sequenceDiagram
    participant GH as GitHub
    participant Know as Knowledge Service
    participant DB as Database
    participant AISvc as AI Service
    
    GH->>Know: POST /webhooks/github (push event delta payload)
    Know->>GH: Pull commit metadata & file changes
    GH-->>Know: File diff payloads returned
    Know->>DB: Queue file delta in indexing database
    Know->>AISvc: Notify Sync Job Queue
    AISvc-->>Know: Acknowledge processing schedule
```

---

# Sequence Diagram 11

## AI Knowledge Indexing
* **Description:** The system parses codebase files and writes semantic embeddings.
* **Preconditions:** Sync delta queues contain un-parsed datasets.

```mermaid
sequenceDiagram
    participant AISvc as AI Service
    participant DB as Database
    
    AISvc->>DB: Poll active delta indexing queue
    DB-->>AISvc: Return file modifications list
    AISvc->>AISvc: Parse code files and syntax trees
    AISvc->>AISvc: Generate vector embeddings tokens
    AISvc->>DB: Update project semantic vector indexes
    AISvc->>DB: Mark queue tasks as completed
```

---

# Sequence Diagram 12

## Ask AI Question
* **Description:** Developer queries the platform from the IDE search console.
* **Preconditions:** User session active; project semantic index healthy.

```mermaid
sequenceDiagram
    actor Dev as Developer
    participant Frontend as IDE Plugin
    participant Search as Search Service
    participant Auth as Authentication Service
    participant AISvc as AI Service
    participant DB as Database
    
    Dev->>Frontend: Input question ("Retry logic details?")
    Frontend->>Search: POST /query (question, JWT)
    Search->>Auth: Validate user session JWT
    Auth-->>Search: Session active (User email returned)
    Search->>AISvc: Query vector search matches
    AISvc->>DB: Query vector matches
    DB-->>AISvc: Matches returned (files, metadata)
    AISvc-->>Search: Context files returned
    Search->>DB: Verify user RBAC permissions for returned files
    DB-->>Search: Permissions verified (Allow access)
    Search->>Search: Ground LLM input & generate answer text
    Search-->>Frontend: Return grounded summary and clickable citations
    Frontend-->>Dev: Display explanation with file links
```

---

# Sequence Diagram 13

## Semantic Search
* **Description:** Developer executes concepts searches within the workspace.
* **Preconditions:** User is logged in.

```mermaid
sequenceDiagram
    actor Dev as Developer
    participant Frontend
    participant Search as Search Service
    participant AISvc as AI Service
    
    Dev->>Frontend: Enter conceptual query ("payment retries")
    Frontend->>Search: GET /search?q=payment (JWT)
    Search->>AISvc: Find semantic vector similarities
    AISvc-->>Search: similarity lists (file paths, similarity score)
    Search->>Search: Apply RBAC access filtering
    Search-->>Frontend: Return ranked relevant files and pages list
    Frontend-->>Dev: Display search results
```

---

# Sequence Diagram 14

## View Dashboard
* **Description:** User checks indexing status and connection health indicators.
* **Preconditions:** User is logged in.

```mermaid
sequenceDiagram
    actor Lead as Tech Lead
    participant Frontend
    participant Proj as Project Service
    participant Know as Knowledge Service
    participant DB as Database
    
    Lead->>Frontend: Open project dashboard
    Frontend->>Proj: GET /projects/metrics
    Proj->>DB: Query active user counts & health indexes
    DB-->>Proj: Metric results returned
    Proj-->>Frontend: Return dashboard summary
    Frontend->>Know: GET /connectors/sync/logs
    Know->>DB: Query recent activity logs
    DB-->>Know: Activity list returned
    Know-->>Frontend: Return sync timeline data
    Frontend-->>Lead: Load status graphs and timeline feed
```

---

# Sequence Diagram 15

## User Management
* **Description:** Admin deactivates a member profile.
* **Preconditions:** Admin is logged in.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Org as Organization Service
    participant DB as Database
    
    OrgAdmin->>Frontend: Select member -> Deactivate
    Frontend->>Org: PATCH /users/status (user_id, status: deactivated)
    Org->>DB: Update user status record in DB
    Org-->>Frontend: User status saved success
    Frontend-->>OrgAdmin: Update active user lists console
```

---

# Error Scenarios

The following diagrams illustrate workflow behaviors under exception scenarios:

## Invalid Login
* **Description:** User enters incorrect credentials.

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant Auth as Authentication Service
    participant DB as Database
    
    User->>Frontend: Enter wrong password
    Frontend->>Auth: POST /login (credentials)
    Auth->>DB: Query credential credentials hash
    DB-->>Auth: Hash mismatch (Access denied)
    Auth->>DB: Increment fail login counts
    Auth-->>Frontend: Return 401 Unauthorized (error message)
    Frontend-->>User: Display "Invalid email or password" warning
```

## Unauthorized Access
* **Description:** An unauthenticated query attempt is rejected.

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant Search as Search Service
    participant Auth as Authentication Service
    
    User->>Frontend: Attempt query search
    Frontend->>Search: POST /query (question, missing JWT)
    Search->>Auth: Validate JWT session token
    Auth-->>Search: Invalid session (401 Unauthorized)
    Search-->>Frontend: Return 401/403 Error
    Frontend-->>User: Redirect to login page
```

## GitHub Connection Failure
* **Description:** GitHub API returns errors during sync configuration.

```mermaid
sequenceDiagram
    actor OrgAdmin as Organization Admin
    participant Frontend
    participant Know as Knowledge Service
    participant GH as GitHub
    participant DB as Database
    
    OrgAdmin->>Frontend: Enter wrong OAuth credentials
    Frontend->>Know: POST /connectors/github (credentials)
    Know->>GH: Validate repository credentials
    GH-->>Know: 401 Unauthorized
    Know->>DB: Log sync failure status (Status: Offline)
    Know-->>Frontend: Return connection credentials error
    Frontend-->>OrgAdmin: Display "GitHub auth failed, verify token" banner
```

## AI Processing Failure
* **Description:** Sync data contains corrupt files that trigger parser exceptions.

```mermaid
sequenceDiagram
    participant AISvc as AI Service
    participant DB as Database
    
    AISvc->>DB: Pull sync queues
    DB-->>AISvc: Return file deltas
    AISvc->>AISvc: Parse file (Parse error: invalid syntax)
    AISvc->>DB: Log exception details (Skip corrupt file)
    AISvc->>DB: Update parser task stats as (Status: Completed-With-Errors)
```

## Search Failure (Context Sparse)
* **Description:** Developer queries information missing from the semantic index.

```mermaid
sequenceDiagram
    actor Dev as Developer
    participant Frontend
    participant Search as Search Service
    participant AISvc as AI Service
    
    Dev->>Frontend: Ask query ("Payroll microservice setup")
    Frontend->>Search: POST /query (question)
    Search->>AISvc: Match similarity values in vector db
    AISvc-->>Search: Similarity confidence below threshold (0.4)
    Search-->>Frontend: Return "context not found" (Hallucination block)
    Frontend-->>Dev: Display "Context not found in indexed repositories"
```

---

# Assumptions

The sequence diagrams rely on the following design assumptions:
* Third-party systems expose stable REST/GraphQL endpoints for ingestion.
* Session tokens (JWT) can be decrypted and verified without database access.
* Database actions execute within normal transactional limits.
* IDE extensions can display formatted markdown search responses.

---

# Constraints

* **Read-Only Scopes:** Under no circumstances do sequences write modifications to external platforms (GitHub/Jira).
* **Latency Limits:** Synchronous search lookup sequences (SD 12, SD 13) must complete execution in less than 2.0 seconds.
* **Domain Check:** Registration is constrained by organization email domain suffix filters.

---

# Risks

### Ingestion Sequence Timeout
* *Risk:* Sync sequence blocks if GitHub/Jira response times lag, delaying delta queues.
* *Mitigation:* Ingest files asynchronously using separate background worker pools.

### Search Access Leak
* *Risk:* User obtains code search responses from repositories they do not have credentials to read on GitHub.
* *Mitigation:* Ensure the Search Service queries the Organization Service to match role permissions before building answer summaries.

---

# Conclusion

These UML sequence diagrams establish the runtime interaction models for the ProjectMind AI platform. By clarifying the message-passing sequences between actors, frontend clients, backend services, and external platforms, this document guides developers through API structure design, helps QA engineers write interface integration tests, and validates architectural boundaries.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Sequence Diagrams Document. |
