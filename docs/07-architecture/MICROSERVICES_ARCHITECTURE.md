# Microservices Architecture

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Architecture / Microservices Architecture |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

## Executive Summary

ProjectMind AI employs a containerized, stateless microservices architecture to manage the secure crawling, indexing, and querying of enterprise codebase metadata. By decoupling core domains—authentication, organization spaces, repository connectors, AI embedding parsers, and search matching pipelines—into distinct service boundaries, the architecture ensures independent deployments, fault isolation, and targeted scalability.

This Microservices Architecture document defines service boundaries, responsibilities, communication patterns, data ownership, resilience strategies, and monitoring requirements that support ProjectMind AI's enterprise-grade deployment.

---

## Architecture Principles

The system architecture adheres to the following principles:

* **Single Responsibility:** Each service owns and executes a single bounded business capability (e.g., authentication, ingestion, vector matching).
* **Loose Coupling:** Services interact only through stable API gateways or message schemas, preventing compile-time dependencies.
* **High Cohesion:** Closely related functional operations are grouped within the same service boundary to limit cross-service latency.
* **Independent Deployment:** Each microservice can be built, tested, and deployed to Kubernetes clusters independently.
* **Fault Isolation:** A failure in an asynchronous connector service does not impact the availability of user authentication or the search Q&A service.
* **Scalability:** Resource-heavy microservices (such as the AI parsing and vector generator service) scale independently of user sessions.
* **Security by Design:** Access boundaries are enforced at the gateway layer, and all service-to-service calls use authorization tokens.

---

## Service Inventory

The functional capabilities of ProjectMind AI are partitioned across the following services:

| Service Name | Responsibility | Bounded Data Owned | External Dependencies |
|---|---|---|---|
| **API Gateway** | Entry point routing, rate limiting, and SSL termination. | None (Session validation cache) | Client Apps (IDE/Web) |
| **Authentication Service** | Identity validation, SAML SSO mapping, and JWT generation. | User credentials, JWT keys, user profiles. | Identity Provider (Okta) |
| **Organization Service** | Manage isolated tenants and user roles mappings. | Tenant structures, role parameters. | None |
| **Project Service** | Organize project workspaces and workspace settings. | Project metadata, workspaces configs. | None |
| **Knowledge Service** | Ingest codebase, tickets, and wikis asynchronously. | API tokens, sync queues logs. | GitHub APIs, Jira Cloud, Confluence REST APIs |
| **AI Service** | Parse codebase structures and generate vector embeddings. | Vector similarity indexes. | Local private LLM / Vector DB |
| **Search Service** | Match natural language queries and enforce RBAC filters. | Search query activity logs. | AI Service, Auth Service |

---

## Service Responsibilities

### API Gateway
* **Purpose:** Route client requests and enforce gateway security rules.
* **Core Responsibilities:** TLS termination, rate-limiting, and routing queries to downstream services.
* **Business Capabilities:** Rate pacing and gateway threat protection.
* **Inputs:** HTTP client queries, token parameters.
* **Outputs:** Forwarded request payloads, *429 Too Many Requests* warnings.
* **Dependencies:** Downstream microservices.
* **Failure Handling:** Returns *504 Gateway Timeout* if downstream targets fail to respond.

### Authentication Service
* **Purpose:** Manage user authentication and secure JWT sessions.
* **Core Responsibilities:** Execute SSO certifications, check passwords, and issue JWT tokens.
* **Business Capabilities:** Identity verification and active session audits.
* **Inputs:** Email, passwords, SAML callback assertions.
* **Outputs:** Secure JWT tokens, user profiles configuration records.
* **Dependencies:** Database, Organization Service.
* **Failure Handling:** Return *401 Unauthorized* if credentials check fails; utilize read-only credentials cache if primary DB is offline.

### Organization Service
* **Purpose:** Govern logical tenant isolation.
* **Core Responsibilities:** Create tenants, process invitations, and map membership roles.
* **Business Capabilities:** Multi-tenant isolation and user governance.
* **Inputs:** Organization settings parameters, user invite lists.
* **Outputs:** Tenant schemas, email invitation triggers.
* **Dependencies:** Database.
* **Failure Handling:** Rejects duplicate organization creation calls; logs admin audit warnings.

### Project Service
* **Purpose:** Establish workspaces grouping code connectors.
* **Core Responsibilities:** Create, edit, and archive project workspaces.
* **Business Capabilities:** Logical project boundary configuration.
* **Inputs:** Project name, workspace parameters, namespace configuration.
* **Outputs:** Workspaces metadata database records.
* **Dependencies:** Database, Organization Service.
* **Failure Handling:** Gracefully rolls back database schema updates if transactions fail.

### Knowledge Service
* **Purpose:** Manage external API connections and ingest metadata.
* **Core Responsibilities:** Store OAuth keys, pull Git commit deltas, and process sync runs.
* **Business Capabilities:** Repository integration and asynchronous data delta download.
* **Inputs:** GitHub/Jira/Confluence API endpoints, tokens, webhook events.
* **Outputs:** Ingested raw source files, ticket texts, wiki records.
* **Dependencies:** Database, External APIs (GitHub, Jira, Confluence).
* **Failure Handling:** Implements exponential back-offs when sync queries fail; logs offline status.

### AI Service
* **Purpose:** Parse codebase elements and build semantic graph files.
* **Core Responsibilities:** Parse codebase structures, generate vector tokens, and update index.
* **Business Capabilities:** Semantic indexing and code relationship modeling.
* **Inputs:** Ingested files, tickets, wikis.
* **Outputs:** Vector indexes database updates.
* **Dependencies:** Vector Database.
* **Failure Handling:** Skips parse errors on corrupt files, logging exception details.

### Search Service
* **Purpose:** Resolve conceptual user queries.
* **Core Responsibilities:** Match queries against vector database indexes and apply RBAC permission filters.
* **Business Capabilities:** Grounded AI Q&A and semantic search lookup.
* **Inputs:** Natural language queries, JWT session profiles.
* **Outputs:** Citations list, grounded text explanations.
* **Dependencies:** AI Service, Authentication Service, Organization Service.
* **Failure Handling:** Returns "context not found" if semantic confidence falls below target threshold.

---

## Service Communication

ProjectMind AI orchestrates service communication using two primary models:

```
   [Synchronous REST]  Client -> API Gateway -> Auth / Search Service
   [Asynchronous Event] GitHub Webhook -> Knowledge Service -> AI Service (Queue)
```

* **Synchronous REST Communication:** Requests requiring real-time response round-trips (such as login queries or IDE search lookups) are routed via HTTPS using JSON payloads. This guarantees low-latency query completions.
* **Asynchronous Event Communication:** Ingestion pipelines and indexing runs are handled asynchronously. Webhook events write delta files in database queues, allowing the AI Service to process embeddings in the background without locking user APIs.
* **Resilience Protocols:** Service connections employ timeout limits (e.g., 5 seconds for user search requests), retries (maximum 3 attempts for transient network lags), and circuit breakers to prevent cascading outages.

---

## Data Ownership

To guarantee tenant isolation and independent scalability, each microservice maintains database ownership boundaries:

| Microservice | Database Ownership | Shared Data Policy |
|---|---|---|
| **Authentication Service** | Relational User Credentials Schema | Shared user metadata sent via token signatures. |
| **Organization Service** | Relational Tenants & Roles Schema | Shared Role IDs to verify RBAC access bounds. |
| **Project Service** | Relational Projects Metadata Schema | Shared Project IDs to group connectors. |
| **Knowledge Service** | Relational Credentials & Sync Log Tables | Private API keys. Ingested delta files sent to AI Service. |
| **AI Service** | High-performance Vector Indexes DB | Private vector embeddings datasets. |
| **Search Service** | Private Search Telemetry Logs DB | No direct read access to other services' schemas. |

---

## External Integrations

ProjectMind AI communicates with external platforms using read-only API connectors:

* **GitHub Integration:** Ingests repository files, Git logs, and pull request comments to map code changes.
* **Jira Integration:** Ingests project requirements, epics, and closed ticket logs to establish functional context.
* **Confluence Integration:** Ingests wikis and setup articles to validate implementation details against architectural guidelines.
* **AI/LLM Provider:** Translates natural language queries and generates grounded context summaries.

---

## Scalability Strategy

The microservices architecture employs five scaling patterns:

* **Horizontal Scaling:** Microservices are containerized and deployed under Kubernetes HPA, scaling instances dynamically based on CPU/GPU usage.
* **Stateless Services:** Processing nodes carry no local transaction memory, routing states to databases or reading from JWT payloads.
* **Load Balancing:** API Gateway and service mesh tools distribute requests uniformly across active nodes.
* **Caching Strategy:** Cache layers store authorization settings and repetitive query results, reducing load on vector databases.
* **Background Processing:** Indexing runs are deferred to worker queues, protecting synchronous search performance.

---

## Resilience Strategy

To ensure system stability, ProjectMind AI integrates resilience protocols:

* **Circuit Breaker:** Disables connectors if third-party APIs return consecutive failures, protecting central services from thread exhaustion.
* **Retry Policy:** Connectors utilize exponential back-off retries for transient HTTP errors.
* **Timeout Handling:** Gateway configurations enforce strict timeout limits (e.g., 2.0s search latencies) to avoid thread blocking.
* **Graceful Degradation:** If the vector database is offline, the system returns a safe warning message rather than crashing the portal.

---

## Security Considerations

Security controls are embedded across the microservices architecture:
* **JWT Authentication:** All client requests must supply a valid JWT session key signed by the Authentication Service.
* **RBAC:** Search result lists are filtered dynamically based on user role settings and source system permissions.
* **Service-to-Service Authentication:** Internal service communication is verified using secure tokens.
* **Secure Communication:** Enforced TLS 1.3 encryption for all data in transit; databases are encrypted at rest using AES-256.
* **Secrets Management:** Integration credentials and API tokens are managed and rotated using secure secrets stores.

---

## Monitoring & Observability

Observability is maintained through four centralized layers:
* **Centralized Logging:** System logs are aggregated into central indexing databases for troubleshooting.
* **Metrics:** Telemetry endpoints export metrics detailing memory usage, sync intervals, and query latency.
* **Distributed Tracing:** Request headers append trace IDs, allowing developers to track execution paths across services.
* **Health Checks:** Kubernetes probes monitor service liveness and readiness states.

---

## Architecture Decision Summary

The architectural choices for ProjectMind AI are summarized below:

| Architectural Decision | Technical Reason | Expected Business Benefit |
|---|---|---|
| **Microservices Style** | Varying resource needs between CPU-heavy vector processing and lightweight auth. | Cost-efficient resource scaling and independent service updates. |
| **Tenant DB Isolation** | Complete logical partition boundaries required for enterprise security. | Zero cross-tenant data contamination risk. |
| **Event-Driven Webhooks** | Ingesting and parsing large codebases are asynchronous, long-running tasks. | Non-blocking user queries and reliable sync retry queues. |
| **In-VPC Private LLM Deployment** | Strict enterprise compliance requirements forbid external cloud code sharing. | Clears corporate security review blocks for client code assets. |

---

## Risks

The implementation of the microservices architecture faces key risks:

### System Complexity overhead
* *Risk:* Managing separate database schemas and deployments increases SRE configuration overhead.
* *Mitigation:* Employ standardized Infrastructure-as-Code templates and automated CI/CD pipelines.

### Data Consistency Lag
* *Risk:* Delays in syncing user role permissions from GitHub/Jira could lead to temporary authorization gaps.
* *Mitigation:* Perform real-time permission validations at the gateway layer for search queries.

---

## Conclusion

The ProjectMind AI Microservices Architecture provides a secure, decoupled, and scalable framework to govern technical context continuity. By dividing capabilities into clear service domains, enforcing logical tenant database isolation, and executing models privately within secure VPC boundaries, this design supports enterprise scaling requirements while protecting corporate intellectual property assets.

---

## Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Microservices Architecture Document. |
