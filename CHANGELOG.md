# Changelog

All notable changes to the ProjectMind AI Platform will be documented in this file.

## [1.0.0] - 2026-07-26

### Added
- Angular 20 Standalone client layout framework with Signal state management.
- Platform telemetry metrics dashboards (Prometheus, Grafana, Loki, Alertmanager).
- Organization management panels and security isolation guards.
- Project Workspace components with dynamic timelines and member drawers.
- Knowledge Library supporting multipart file uploads and previews.
- AI Chat Console supporting active conversation feeds and citations views.
- Production Helm Charts for Kubernetes deployments.
- GitHub Actions CI/CD workflows for compilation, testing, and container registry publishing.

### Fixed & Patched
- **Database Environment Configuration**: Resolved PostgreSQL datasource property binding issues across all microservice profiles to read `DATABASE_URL` settings.
- **Gateway Routing**: Adjusted Kubernetes profile routing configurations in `api-gateway` to route directly via K8s service DNS instead of failing registry lookups.
- **OpenAPI Integration**: Fixed compilation failures in `auth-service` by declaring the `springdoc-openapi-starter-webmvc-ui` dependency globally.
- **JJWT API Compatibility**: Replaced deprecated `.allowedClockSkew(...)` with `.clockSkewSeconds(...)` for JWT verification.
- **Microservices Unit Tests**: Configured `spring-boot-starter-test`, `spring-security-test`, and `h2` database globally in the parent POM to enable unit testing.
- **Generics Compilation**: Fixed Mockito type assertions in `RoleAssignmentServiceTest` by leveraging loosely-typed `doReturn` stubs.
- **Parameter Name Reflection**: Enabled parameter name preservation via the `-parameters` compiler configuration in `pom.xml` to fix controller test mapping errors.
- **Database Test Context Isolation**: Added `src/test/resources/application.yml` inside `organization-service` to override `ddl-auto: validate` with `create-drop` to support clean H2 testing.
- **Mock Assertions**: Updated Mockito event verification to use explicit type matchers (`any(Object.class)`) to resolve class loading conflicts.
- **Git Event Compilation**: Added the missing `RepositoryDeletedEvent` class to resolve compilation failures in `project-service`.
