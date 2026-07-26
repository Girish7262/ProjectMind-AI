# Security Architecture

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Architecture / Security Architecture |
| Version | 1.0.0 |
| Status | Published |
| Owner | Portfolio Developer |
| Reviewer | Portfolio Developer |
| Last Updated | 2026-07-22 |

---

# Executive Summary

ProjectMind AI is designed to manage the indexing and retrieval of highly sensitive enterprise codebase assets, Jira tickets, and Confluence wikis. To protect this intellectual property, the platform enforces a security strategy centered on data isolation, zero outbound exposure of source files, and permission alignment.

Operating as a read-only context retrieval layer, the system leverages Single Sign-On (SSO) authentication, signed JWT sessions, and role-based access control filters (RBAC) synchronized from source systems. This Security Architecture document establishes the security principles, authentication models, cryptographic controls, threat definitions, and compliance boundaries governing the platform.

---

# Security Principles

The security design of ProjectMind AI is built upon five architectural principles:

* **Zero Trust:** Never trust, always verify. Every request—whether originating from the web browser dashboard, the IDE plugin, or service-to-service internal calls—must validate session tokens and identity claims before processing data.
* **Least Privilege:** Users, services, and integrated connectors are granted only the minimum access permissions necessary to perform their roles. Integration credentials are bound to read-only API scopes.
* **Defense in Depth:** Security controls are implemented across multiple layers of the stack, including perimeter proxies, API gateway rate pacing, microservice JWT checks, database security groups, and network subnets.
* **Secure by Default:** Default configurations enforce strict domain suffix filters, short-lived session expiries, and disabled global administrative setups until explicitly configured.
* **Principle of Least Knowledge:** The platform indexes codebase metadata and relations on-the-fly. It does not store copy versions of the entire source code files, ensuring AI generation contexts do not persist beyond the query lifecycle.

---

# Authentication Architecture

ProjectMind AI manages user authentication and active sessions through the following controls:

* **JWT Authentication:** Upon successful login via corporate directories, the Authentication Service issues a cryptographically signed JSON Web Token (JWT). Subsequent requests from IDE extensions and browsers must append this token in HTTP Authorization headers.
* **SSO Integration:** Primary authentication is delegated to enterprise Identity Providers (IdP) using SAML 2.0 or Okta integrations. This eliminates password storage on local databases, leveraging corporate MFA policies.
* **Token Lifecycle:** JWT session tokens are assigned short lifespans (defaulting to 24 hours).
* **Refresh Tokens (Future):** Future iterations will support secure refresh token rotations, storing credentials in secure browser caches.
* **Session Management:** Passive sessions are automatically terminated by client cookies configurations after a predefined period of inactivity.

---

# Authorization Architecture

Authorization boundaries isolate tenant data and respect developer access rights:

* **Role-Based Access Control (RBAC):** Users are assigned to predefined local roles (Engineer, Tech Lead, Admin) that define platform capabilities (e.g., executing queries, managing connectors).
* **Organization-Level Access:** Logical tenant isolation is enforced at the database layer. Search queries and configurations are filtered by the tenant identifier mapped to the user's JWT.
* **Project-Level Permissions:** Project workspace boundaries restrict access to authorized engineering teams, preventing unauthorized internal access.
* **AI Access Restrictions:** Before generating a query response, the Search Service checks the user's active GitHub/Jira access token against the list of retrieved context files. It dynamically filters out results from directories the user is unauthorized to view.

---

# API Security

The API Gateway enforces perimeter defense rules:

* **HTTPS Enforcement:** TLS 1.3 encryption is mandatory for all API requests. Non-HTTPS requests are rejected at the Nginx edge.
* **Authentication & Authorization validation:** The API Gateway validates JWT signatures and parses role permissions headers before routing calls to downstream microservices.
* **Rate Limiting:** Gateway rate limit policies throttle query traffic based on IP address and JWT identifiers, preventing Denial of Service (DoS) attacks and API abuse.
* **Input Validation:** Input parameters are validated for length, characters, and syntax formats to block injection payloads.
* **API Versioning:** APIs utilize version paths (e.g., `/api/v1/`) to support backward compatibility and clean security update rollouts.

---

# Service-to-Service Security

Internal communication within the application subnet is secured using the following patterns:

* **Secure Internal Communication:** Microservices communicate using private virtual networks, preventing external direct access.
* **Service Identity:** Services verify calls using secure authorization tokens passed between internal components.
* **Secrets Management:** Ingestion API keys, database credentials, and OAuth tokens are stored in secure vault managers, injected as environment variables at runtime.
* **Certificate Management:** System administrators rotate internal TLS certificates regularly to maintain transport security.

---

# Data Security

Cryptographic controls protect tenant data at rest and in transit:

* **Encryption in Transit:** All traffic between client interfaces, proxies, microservices, and external APIs is encrypted using TLS 1.3.
* **Encryption at Rest:** PostgreSQL databases, pgvector vector indexes, and staging local file storages are encrypted using AES-256 standards.
* **Password Hashing:** Relational user tables store credentials using bcrypt with a high work factor, preventing cleartext leaks.
* **Sensitive Data Protection:** API credentials and OAuth tokens connected to GitHub/Jira are encrypted at rest using system-managed keys.
* **Backup Encryption:** Automated database backups and index snapshots are encrypted prior to uploading to offsite storage.

---

# Infrastructure Security

Infrastructure security secures the physical container runtime:

* **Docker Security:** Microservices run within minimal container base images, with root execution disabled.
* **Container Isolation:** Docker virtual networks isolate database containers from frontend proxies.
* **Reverse Proxy Security:** Nginx acts as the perimeter barrier, handling static web files serving and blocking direct access to Spring Boot APIs.
* **Firewall Strategy:** Security groups restrict ingress access to ports 80/443, blocking internal database ports (5432, 6379) from external subnets.
* **Network Segmentation:** System components are segmented into public ingress tiers, private application tiers, and isolated database tiers.

---

# External Integration Security

Connecting to third-party APIs requires specific security controls:

* **GitHub & Jira Connectors:** Access is secured using read-only OAuth tokens or API keys, limiting permissions to repository metadata reads.
* **Confluence Connector:** Integrations use read-only Space ID credentials, preventing write access to wiki documents.
* **OpenAI / Azure OpenAI APIs:** To prevent IP leaks, ProjectMind AI supports routing contexts to Azure OpenAI instances under private corporate networks, ensuring code metadata is never ingested into public training models.

---

# Audit & Monitoring

Observability is maintained through three security tracking systems:

* **Audit Logging:** Immutably logs administrative actions, credential changes, role modifications, and project workspace setups.
* **Security Logging:** Logs login failures, session expiration warnings, and unauthorized query access attempts (*403 Forbidden*).
* **Monitoring & Alerting:** Active metrics monitor auth latencies and sync errors, alerting the security team of anomaly events.

---

# Threat Model

The platform mitigates key cyber threats according to the following matrix:

| Security Threat | System Impact | Mitigation Strategy |
|---|---|---|
| **Credential Theft** | Unauthorized access to user settings or search portal. | SAML SSO delegation, short-lived JWT signatures, and MFA enforcement. |
| **Unauthorized Access** | User searches code directories they lack permissions to read. | Dynamic RBAC matching user's active GitHub/Jira token group before generating answers. |
| **Injection Attacks** | SQL or prompt injections corrupt database files. | Strict input validation on APIs, parameterized queries, and context grounding limits. |
| **Cross-Site Scripting (XSS)** | Attacker runs scripts inside the client browser. | Angular output escaping and strict Content Security Policies (CSP) filters. |
| **CSRF Attacks** | Malicious third-party executes commands on user session. | Secure JWT storage in HttpOnly cookies and validation of custom headers at the API Gateway. |
| **Corporate Data Leakage** | Codebase logic is shared with public LLMs. | Route AI queries to private VPC models, blocking external cloud access. |
| **API Abuse (DoS)** | Excessive query volume blocks portal access. | Enforce rate limiting quotas on Nginx proxy and API Gateway. |
| **Insider Threats** | Malicious admin accesses search history. | Immutable audit logging of all admin actions, query logs isolation. |

---

# Security Best Practices

ProjectMind AI recommends the following enterprise security best practices:
* Enforce Multi-Factor Authentication (MFA) across all identity SSO directories.
* Restrict API gateway endpoints to internal corporate Virtual Private Network (VPN) subnets.
* Enforce monthly rotations of OAuth connector keys connected to GitHub and Jira.
* Perform weekly vulnerability scans of container images to patch dependencies.
* Run automated static application security testing (SAST) in CI/CD pipelines.

---

# Compliance Considerations

The platform design addresses key compliance frameworks:
* **OWASP Top 10:** Enforce parameter sanitization, secure session keys, and strict RBAC to prevent injection and permission errors.
* **GDPR Compliance:** Support user "right to be forgotten" requests, allowing admins to purge profile data and search logs from relational tables.
* **Secure Development:** Code reviews, automated testing, and secure dependency management guide engineering workflows.
* **Enterprise Auditing:** Audit logs are exportable by compliance officers for audit reviews.

---

# Security Risks

Operational security is subject to key risks:

### API Key Theft
* *Risk:* Compromised GitHub/Jira integration keys allow unauthorized read access to corporate repositories.
* *Mitigation:* Store tokens in secure KMS vaults, rotates keys monthly, and limit token permissions to read-only metadata scopes.

### Outdated User Privileges
* *Risk:* Terminated employees retain access to ProjectMind AI search portal due to permission synchronization lags.
* *Mitigation:* Query active user roles from SAML SSO directories on every JWT session validation.

---

# Conclusion

The Security Architecture of ProjectMind AI established a Zero-Trust, isolated framework to protect the organization's intellectual property assets. By enforcing SAML SSO logins, signed JWT session keys, dynamic RBAC checks, transport encryption, and secure VPC deployments, the platform mitigates corporate cyber threats while supporting secure, scalable developer search workspaces.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-22 | Developer / Architect | Initial creation of the Security Architecture Document. |
