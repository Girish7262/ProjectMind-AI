# Operational Runbook - Knowledge Service

This runbook provides step-by-step diagnostic and remediation instructions for typical production incidents in the Knowledge Service.

## 1. Incident: High Memory Usage (OOM Warnings)

### Symptoms
- Pods crash with exit code `137` (Out of Memory).
- Logs show `java.lang.OutOfMemoryError`.

### Action Plan
1. Check container memory limit settings and actual usage:
   ```bash
   kubectl top pod -n acciobuild | grep knowledge-service
   ```
2. Verify that JVM settings in the Dockerfile conform to Kubernetes resource limits. If resource limits are restricted to 1024Mi, verify that cgroups correctly set:
   ```bash
   # Env config
   JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
   ```
3. Generate a thread/heap dump if necessary using JDK troubleshooting utilities inside the container.

---

## 2. Incident: Circuit Breaker Open (`projectService` or `organizationService`)

### Symptoms
- Downstream HTTP requests to Feign Clients fail immediately with `CallNotPermittedException`.
- Error logs indicate that Resilience4j Circuit Breakers are in `OPEN` state.

### Action Plan
1. Check down services status (e.g. `project-service` or `auth-service` / `organization-service` status):
   ```bash
   kubectl get pods -n acciobuild
   ```
2. Check the endpoint latency or error rate.
3. Once the target downstream service is restored and passes health checks, the circuit breaker will automatically transition to `HALF_OPEN` after the wait duration state (15s) and start permitting traffic again.

---

## 3. Incident: Embedding Queue Size Backlog Alert

### Symptoms
- Prometheus alarm `EmbeddingQueueBacklogHigh` is triggered.
- Queue size gauge `acciobuild.knowledge.embedding.queue.size` is steadily climbing.

### Action Plan
1. Verify if `EmbeddingJobScheduler` is active: check scheduler thread logs.
2. Check if database connections are exhausted: check Hikari connection pool metrics.
3. Scale up the pods to parallelize background processing if database resources allow:
   ```bash
   kubectl scale deployment/knowledge-service --replicas=4 -n acciobuild
   ```
