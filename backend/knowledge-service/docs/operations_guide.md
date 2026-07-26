# Operations Guide - Knowledge Service

This guide provides administrators and SREs with instructions for day-to-day operations, scaling strategies, backup/restore procedures, and disaster recovery configurations.

## 1. Dynamic Scaling Strategy

Replicas scale automatically based on target CPU utilization (80%) managed by the Horizontal Pod Autoscaler (HPA).
To manually override pod sizing rules or inspect target settings:

```bash
# View active HPA status
kubectl get hpa -n acciobuild

# Manually scale deployment
kubectl scale deployment/knowledge-service --replicas=4 -n acciobuild
```

---

## 2. Backup & Restore Procedures

### Database Backups (PostgreSQL)
Ensure pg_dump commands are executed regularly against the Postgres service:

```bash
# Export schema and database data
pg_dump -h postgres-service.acciobuild.svc.cluster.local -U postgres -d acciobuild_knowledge > knowledge_backup_$(date +%F).sql
```

### Database Restoration
```bash
# Restore schema
psql -h postgres-service.acciobuild.svc.cluster.local -U postgres -d acciobuild_knowledge < knowledge_backup.sql
```

---

## 3. Disaster Recovery (DR)

### State Restoration
1. If the database crashes, restore the persistent volume from snapshot.
2. In case of message delivery gaps (Kafka or transactional outbox queue lags), trigger manual outbox processor scan:
```bash
# Invoke outbox processor manually via Admin utility endpoint if needed, or restart outbox scheduler
kubectl rollout restart deployment/knowledge-service -n acciobuild
```
