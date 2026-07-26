package com.acciobuild.knowledge.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enterprise service capturing custom domain metrics via Micrometer and mapping telemetry stats to Prometheus.
 */
@Component
public class KnowledgeMetricsService {

    private final MeterRegistry registry;
    private final AtomicLong embeddingQueueSizeGauge;

    public KnowledgeMetricsService(MeterRegistry registry) {
        this.registry = registry;
        this.embeddingQueueSizeGauge = registry.gauge("acciobuild.knowledge.embedding.queue.size", new AtomicLong(0));
    }

    public void recordDocumentCreated(String format, String visibility) {
        Counter.builder("acciobuild.knowledge.documents.created")
                .tag("format", format != null ? format : "UNKNOWN")
                .tag("visibility", visibility != null ? visibility : "UNKNOWN")
                .description("Number of knowledge documents created")
                .register(registry)
                .increment();
    }

    public void recordVersionCreated() {
        Counter.builder("acciobuild.knowledge.versions.created")
                .description("Number of document versions created")
                .register(registry)
                .increment();
    }

    public void recordSearchRequest(long durationMs) {
        Timer.builder("acciobuild.knowledge.search.requests")
                .description("Latency of searchable index requests")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordChunkPrepared(int chunkCount, long durationMs) {
        Counter.builder("acciobuild.knowledge.chunks.prepared")
                .description("Total number of chunks prepared")
                .register(registry)
                .increment(chunkCount);

        Timer.builder("acciobuild.knowledge.chunks.duration")
                .description("Time taken to run chunk preparation")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordIndexPrepared(long durationMs) {
        Timer.builder("acciobuild.knowledge.indexing.duration")
                .description("Time taken to index full text search records")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordEmbeddingQueueSize(long size) {
        this.embeddingQueueSizeGauge.set(size);
    }

    public void recordOutboxPublished(String eventType, boolean success) {
        Counter.builder("acciobuild.knowledge.outbox.published")
                .tag("eventType", eventType)
                .tag("status", success ? "SUCCESS" : "FAILURE")
                .description("Count of transactional outbox logs processed")
                .register(registry)
                .increment();
    }

    public void recordLifecycleTransition(String fromState, String toState) {
        Counter.builder("acciobuild.knowledge.lifecycle.transitions")
                .tag("from", fromState)
                .tag("to", toState)
                .description("Knowledge lifecycle state machine transitions count")
                .register(registry)
                .increment();
    }
}
