# Release Notes - ProjectMind AI v1.0.0

General Availability (GA) of ProjectMind AI, the Personal AI Knowledge Continuity & Retrieval Platform. ProjectMind AI bridges information tool silos, allowing developers to secure codebase knowledge and query it via Retrieval-Augmented Generation (RAG).

## Core Features Included in v1.0.0
1. **API Gateway & Routing**: Spring Cloud Gateway configurations with circuit breakers, tenant header propagation, and Redis rate limiters.
2. **Angular 20 Standalone Client**: Standalone components, state signals management, SCSS styling system, workspace logs, knowledge libraries, and responsive AI chat streams.
3. **Multi-tenant Isolation**: Tenant-data segregation enforced via dynamic JPA specification filters using organization-bounded database schemas.
4. **Outbox Pattern Events**: Transactional outbox log tables and Apache Kafka messaging blocks to publish state changes.
5. **Containerized Orchestration**: Optimized multi-stage Dockerfiles and single-trigger Docker Compose deployment configurations.
6. **Kubernetes Deployment**: Helm charts supporting Horizontal Pod Autoscalers, ingress paths, and service configurations.
7. **Observability Stack**: Prometheus metrics scraping, Grafana dashboards, Loki log collectors, and Alertmanager incident alerts.

## Resolved Configuration & Build Verification Patches
- Resolved database environment properties mismatch across profiles to allow single-command docker-compose spin-ups.
- Standardized API Gateway routing to resolve service lookups via direct Kubernetes DNS.
- Patched compiler and testing libraries, resolving Swagger/OpenAPI annotation mappings and JUnit 5/Mockito context errors.
- Verified compilation and test executions: **100% of unit and integration test specs pass successfully in both the frontend and backend microservices reactors.**

## Operational Recommendations
- **JVM Runtime**: Java 17/21 SDK.
- **NodeJS Environment**: Node 18+ with npm 9+.
- **Database Engine**: PostgreSQL 15+ with the `pgvector` extension enabled.
