# Monitoring Guide - Knowledge Service

This guide lists observability metrics, health checkpoints, and Prometheus scrape target definitions.

## 1. Health Probe Checkpoints

Kubernetes utilizes these probes to verify container status:
- **Liveness Endpoint**: `http://<host>:8084/actuator/health/liveness` (Verifies JVM and DB connectivity)
- **Readiness Endpoint**: `http://<host>:8084/actuator/health/readiness` (Verifies outbox, search index, chunk preparation, and embedding queue health)

---

## 2. Micrometer Custom Metrics

The service registers the following custom metrics in Prometheus format:
- `acciobuild.knowledge.documents.created`: Total number of documents provisioned. Tags: `format`, `visibility`.
- `acciobuild.knowledge.versions.created`: Count of new document versions created.
- `acciobuild.knowledge.search.requests`: Timer measuring latency of dynamic search queries.
- `acciobuild.knowledge.chunks.prepared`: Total count of text chunks split.
- `acciobuild.knowledge.chunks.duration`: Timer measuring sliding-window partition latency.
- `acciobuild.knowledge.indexing.duration`: Timer measuring full-text indexing database updates.
- `acciobuild.knowledge.embedding.queue.size`: Gauge indicating active size of the embedding jobs queue.
- `acciobuild.knowledge.outbox.published`: Count of transactional outbox logs processed. Tags: `eventType`, `status`.
- `acciobuild.knowledge.lifecycle.transitions`: Count of document state transitions. Tags: `from`, `to`.

---

## 3. Prometheus Alerting Rules

Define the following Prometheus rules for alerting:

```yaml
groups:
- name: knowledge-service-alerts
  rules:
  - alert: KnowledgeDatabaseDown
    expr: up{job="knowledge-service"} == 0 or spring_data_repository_invocations_seconds_count{repository="KnowledgeDocumentRepository"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Database connection failed for Knowledge Service"

  - alert: EmbeddingQueueBacklogHigh
    expr: acciobuild_knowledge_embedding_queue_size > 500
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High embedding jobs backlog"
```
