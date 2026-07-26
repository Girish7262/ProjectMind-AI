# High Level Design

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | System Design / High Level Design |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

# Executive Summary

ProjectMind AI is designed with a modern, secure, and decentralized architecture style to handle the extraction, indexing, and retrieval of enterprise project context. The platform uses a microservices model combined with event-driven triggers to process codebase files, Jira ticket histories, and Confluence spaces asynchronously.

Operating strictly as a read-only metadata ingestion layer, the system keeps codebase assets safe through isolated tenant databases, single-sign-on validation, and private cloud model execution. This High Level Design (HLD) outlines the architectural objectives, component definitions, request routing flows, security controls, and scaling strategies that establish ProjectMind AI's target architecture.

---

# Architectural Goals

The platform's high-level design is guided by six primary architectural objectives:

* **Scalability:** Capable of indexing massive multi-repository organizations containing millions of lines of code and historical tickets without performance degradation.
* **Maintainability:** Loose coupling of microservices enables independent updates, deployments, and testing loops without system-wide regressions.
* **Security:** Complete logical isolation of tenant databases, secure API token storage, and strict mapping of role-based access filters (RBAC) synchronized from source systems.
* **Reliability:** Asynchronous background ingestion workers process delta queues safely, handling third-party system outages via automatic query retries.
* **Extensibility:** Modular connectors and plug-in APIs enable future tool additions (such as Notion, GitLab) without modifying the core query engine.
* **High Availability:** Redundant service container deployments, load balancers, and stateless design parameters ensure continuous platform accessibility.

---

# Architecture Style

ProjectMind AI is built on a **Microservices Architecture** combined with **RESTful and Event-Driven Communication** protocols:

* **Stateless Microservices:** The platform decomposes system capabilities into small, focused, stateless microservices that run independently. This isolates failures and allows resource-heavy components (like the AI Knowledge Engine) to scale dynamically without affecting authentication services.
* **Event-Driven and RESTful Routing:** Synchronous operations (such as user logins and search queries) use RESTful APIs. Asynchronous operations (such as knowledge ingestion and vector updates) are event-driven, triggered by GitHub push webhooks or scheduled synchronization crons to prevent connection blocking.
* **Suitability:** Large enterprise customers demand high data isolation and code privacy. A microservices model allows organizations to deploy and run the AI indexing engine and vector search databases locally inside their secure private network (VPC) while using lightweight IDE extensions.

---

# Major Components

ProjectMind AI partitions system behavior across the following components:

### Angular Frontend
* *Purpose:* Provide the configuration dashboard console and workspace settings portal.
* *Responsibilities:* Authenticate user login sessions, manage project configurations, and render index health metrics.
* *Dependencies:* API Gateway, Authentication Service, Project Service.

### API Gateway
* *Purpose:* Orchestrate and route request traffic from client interfaces to downstream services.
* *Responsibilities:* Enforce rate limiting, validate JWT headers, perform TLS termination, and direct queries.
* *Dependencies:* All internal microservices.

### Authentication Service
* *Purpose:* Manage identity validation and user session states.
* *Responsibilities:* Integrate with SAML SSO/Okta identity directories, generate secure JWT tokens, and manage user profile records.
* *Dependencies:* Database, Organization Service.

### Organization Service
* *Purpose:* Govern tenant metadata boundaries.
* *Responsibilities:* Initialize tenant database partitions and manage user roles and membership listings.
* *Dependencies:* Database.

### Project Service
* *Purpose:* Define workspace boundaries to group connectors.
* *Responsibilities:* Create, edit, and archive project workspaces.
* *Dependencies:* Database, Organization Service.

### Knowledge Service
* *Purpose:* Manage external connector integrations and data ingestion delta updates.
* *Responsibilities:* Store API credentials securely, receive Git/Jira webhooks, and schedule ingestion sync runs.
* *Dependencies:* Database, AI Service, External Systems.

### AI Service
* *Purpose:* Process codebase and documentation inputs to build semantic indexes.
* *Responsibilities:* Run code syntax parsing, generate vector embedding tokens, and link requirements to code paths.
* *Dependencies:* Database, Knowledge Service.

### Search Service
* *Purpose:* Serve user natural language queries and return citations.
* *Responsibilities:* Run vector matching, apply user RBAC access filters, and format grounded Q&A text outputs.
* *Dependencies:* AI Service, Authentication Service.

### Database
* *Purpose:* Store system configurations, metadata, and semantic indices.
* *Responsibilities:* Maintain relational settings databases and handle vector indexes separately to guarantee low-latency search lookups.
* *Dependencies:* None.

### External Integrations
* *Purpose:* Connect read-only pipelines to source tools.
* *Responsibilities:* Retrieve codebase updates, tickets, and wiki pages.
* *Dependencies:* None.

---

# High-Level Component Interaction

ProjectMind AI orchestrates component communication to process query lookups:

```
   [IDE Search] -> API Gateway -> Search Service -> AI Service -> Vector Index
                        |              |
                [Auth Service]   [Org Service] (RBAC check)
```

1. **Authentication:** The developer submits a search query via the IDE plugin. The API Gateway intercepts the request and verifies the user's JWT session with the Authentication Service.
2. **Access Evaluation:** Once authenticated, the gateway forwards the query to the Search Service. The Search Service queries the Organization Service to verify the user's roles and access levels.
3. **Query Resolution:** The Search Service queries the AI Service to process vector matching against the target project index. The Search Service filters out results the user is unauthorized to see (RBAC) and generates a grounded explanation citing the exact codebase files (`file:///...`) and Jira ticket IDs, returning the answer to the IDE plugin.

---

# External Integrations

ProjectMind AI integrates with external enterprise platforms using secure, read-only API connectors. The details of these integrations are as follows:

### GitHub Integration
* **Purpose:** Ingest codebase syntax structure, version histories, and pull request discussion logs to analyze the source code layer.
* **Data Exchanged:** Codebase file contents (text-parseable files), commit metadata, Git branch histories, and pull request reviewer logs.
* **Business Value:** Provides deep visibility into the current-state codebase implementation and developer intent, aligning the semantic index with actual file modifications.

### Jira Integration
* **Purpose:** Ingest user stories, bug reports, and requirement epics to establish functional business context.
* **Data Exchanged:** Jira ticket titles, ticket description texts, status changes, assignee names, and link mappings to code reviews.
* **Business Value:** Bridges code implementation with original functional requirements, enabling developers to understand *why* logic was built.

### Confluence Integration
* **Purpose:** Ingest organizational wikis, runbooks, and design guides to validate implementation against documented architecture rules.
* **Data Exchanged:** Wiki pages, directory structures, reference links, and document revision histories.
* **Business Value:** Captures high-level tribal setup knowledge and architectural guidelines, accelerating new hire onboarding and standard compliance.

---

# Deployment Overview

The platform is deployed across a multi-layered infrastructure model to support scalability and isolation:

* **Client Layer:** Web portal interface (Angular Frontend) and IDE plugins (e.g., VS Code or JetBrains extensions) querying APIs.
* **Application Layer:** Stateless microservices deployed in containers (e.g., Kubernetes pods) behind an API Gateway and load balancers, scaling dynamically.
* **Data Layer:** Isolated database partitions storing relational configuration metadata and vector search indexes.
* **External Services:** Customer-managed Single Sign-On identity directories and target source platforms (GitHub Enterprise, Jira Cloud, Confluence APIs).

---

# Security Overview

Security is a primary design boundary for enterprise deployment:

* **Authentication:** Mandatory integration with SAML 2.0 or Okta SSO, issuing secure session JWT keys.
* **Authorization:** Role-based access control (RBAC) checks that dynamically sync and match repository permissions defined on GitHub and Jira.
* **Secure Communication:** Enforced HTTPS transit using TLS 1.3 standards; all configuration databases and vector indexes are encrypted at rest using AES-256.
* **Audit Logging:** Asynchronous logging of all user searches, logins, configuration adjustments, and API credential changes.

---

# Scalability Strategy

To handle enterprise codebase scales, ProjectMind AI employs four scaling strategies:

* **Horizontal Scaling:** Stateless microservice containers scale horizontally under Kubernetes to handle traffic peaks.
* **Stateless Microservices:** Services carry no local cache states, pulling transaction context from DB parameters or JWT token payloads.
* **Load Balancing:** Dynamic routing of query payloads across redundant container zones.
* **Caching Strategy:** Cache layers configured on the Search Service and IDE plugin cache repetitive queries, minimizing vector database hits.

---

# Availability Strategy

System availability is protected by redundant infrastructure and recovery plans:

* **Fault Tolerance:** Circuit breaker patterns isolate third-party connector issues, preventing sync timeouts from blocking search queries.
* **Data Backup:** Automated daily backups of configuration databases and vector search files.
* **Disaster Recovery:** Multi-availability zone database clustering to support automated recovery and failover.
* **System Monitoring:** Active monitoring of query latency, ingestion queues, and connector health metrics.

---

# Design Assumptions

The high-level architecture relies on the following design assumptions:
* Third-party systems expose stable REST or GraphQL APIs to retrieve code, tickets, and wiki pages.
* Source systems support OAuth or API token authentication mechanisms.
* Code comments, ticket details, and wikis are stored in text-parseable formats.
* Enterprise customers use centralized identity systems for user authentication.

---

# Constraints

The platform design must operate within the following constraints:

### Business Constraints
* Delivery dates must align with pilot validation milestones.
* Budgets are constrained by allocated startup seed capital.

### Technical Constraints
* **Read-Only Enforced Constraint:** No API scopes can authorize write permissions to source systems.
* **IDE Latency Targets:** Search query returns must load in less than 2.0 seconds (P95) to preserve developer flow state.

### Integration Constraints
* Ingestion pipelines must limit query throughput to prevent rate-limit throttling by GitHub/Jira APIs.
* Vector parsing is restricted to text-parseable programming and document formats.

---

# Risks

Architectural execution faces key technical and operational risks:

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

This High Level Design establishes the architectural foundation for ProjectMind AI. By separating capabilities into decoupled, stateless microservices and enforcing security constraints (SSO, RBAC, VPC model hosting), this design ensures that the platform scales reliably across large codebases while protecting the organization's intellectual property assets.

---

## Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the High Level Design Document. |
