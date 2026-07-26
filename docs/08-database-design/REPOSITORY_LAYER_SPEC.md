# Repository Layer Specification

## Document Metadata

| Metadata Field | Value |
|---|---|
| Document Type | Database Design / Repository Layer Specification |
| Version | 1.0.0 |
| Status | Published |
| Owner | Principal Java Architect |
| Reviewer | Developer / Principal Architect |
| Last Updated | 2026-07-23 |

---

# Executive Summary

This Repository Layer Specification defines the Spring Data JPA repository interfaces, custom queries, projections, transaction boundaries, and performance optimization parameters governing the Auth Service data tier. By structuring query methods, entity graphs, and dynamic search specifications, this layer enables high-concurrency lookups while keeping database operations performant and isolated.

---

# Query Design & Custom Repositories

To query transactional tables safely and prevent N+1 select loops, the Auth Service utilizes specific optimizations:

* **Entity Graphs:** Exposes `@EntityGraph` mappings on user queries to eagerly fetch role configurations during logins, preventing lazy evaluation errors.
* **Projections:** Exposes lightweight interfaces to fetch user profile details without loading entire entities or password hash strings.
* **Modifying Queries:** Uses `@Modifying` and `@Query` annotations for update actions (e.g. lockout triggers, login history audits), ensuring updates execute as single SQL queries.

---

# Projection Interfaces

Projections allow services to fetch read-only details without loading database rows:

### `UserSummary.java`
```java
package com.projectmind.auth.repository.projection;

import java.util.UUID;

/**
 * Read-only projection interface returning user details without password hashes.
 */
public interface UserSummary {
    UUID getId();
    String getEmail();
    String getUsername();
    String getFirstName();
    String getLastName();
    String getStatus();
    UUID getOrganizationId();
}
```

---

# Transaction Management & Isolation

Repositories must adhere to strict transaction demarcation rules:

* **Read-Only Transactions:** Retrieve queries (select statements) must be annotated with `@Transactional(readOnly = true)`. This allows Hibernate to skip dirty-checking checks, reducing memory usage.
* **Write Transactions:** Modifying operations (inserts, updates, deletes) must be annotated with `@Transactional`.
* **Transaction Isolation:** Keep isolation levels at PostgreSQL defaults (`ISOLATION_READ_COMMITTED`) to prevent dirty reads. High-concurrency operations use Redis locks to prevent race conditions.

---

# Performance Tuning Guidelines

To optimize query latency, repositories implement four guidelines:

* **Index Mappings:** Ensure searches (queries filtered by email, username, token) match PostgreSQL database indexes.
* **Batch Fetching:** Configure batch fetching properties (`hibernate.default_batch_fetch_size: 50`) to optimize collection loads.
* **Specification Filtering:** Expose specification criteria helpers to build search predicates dynamically.
* **Limit Result Set Sizes:** Use Pageable objects to limit matching return bounds, preventing memory issues.

---

# Conclusion

The Repository Layer Specification outlines Spring Data JPA repositories, custom query mappings, projections, transaction boundaries, and performance optimization rules. Enforcing entity graphs, modifying queries, projections, and read-only annotations ensures fast query response times.

---

# Revision History

| Version | Date | Author / Role | Summary of Changes |
|---|---|---|---|
| 0.1.0 | 2026-07-23 | Backend Tech Lead | Initial creation of the Repository Layer Specification. |
