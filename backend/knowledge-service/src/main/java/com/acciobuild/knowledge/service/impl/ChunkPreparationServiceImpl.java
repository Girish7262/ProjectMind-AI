package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.knowledge.domain.event.KnowledgeChunkPreparedEvent;
import com.acciobuild.knowledge.domain.model.DocumentIndexMetadata;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import com.acciobuild.knowledge.domain.repository.DocumentIndexMetadataRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentChunkRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.exception.KnowledgeDocumentNotFoundException;
import com.acciobuild.knowledge.service.ChunkPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation executing sliding-window chunk partitioning and metadata updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkPreparationServiceImpl implements ChunkPreparationService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDocumentChunkRepository chunkRepository;
    private final DocumentIndexMetadataRepository metadataRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void prepareChunks(UUID documentId, String content) {
        log.info("Partitioning document content into text chunks for ID: {}", documentId);
        KnowledgeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Document not found."));

        if (content == null || content.isBlank()) {
            return;
        }

        // Clean previous chunks first to allow clean re-chunking
        List<KnowledgeDocumentChunk> old = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
        chunkRepository.deleteAll(old);

        // Simple sliding-window chunk builder
        // Chunk size: 500 chars, Overlap: 100 chars
        int chunkSize = 500;
        int overlap = 100;
        List<String> fragments = new ArrayList<>();
        int length = content.length();

        int start = 0;
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            fragments.add(content.substring(start, end));
            if (end == length) {
                break;
            }
            start = start + (chunkSize - overlap);
        }

        int idx = 0;
        for (String frag : fragments) {
            KnowledgeDocumentChunk chunk = new KnowledgeDocumentChunk();
            chunk.setId(UUID.randomUUID());
            chunk.setDocument(doc);
            chunk.setChunkIndex(idx++);
            chunk.setContent(frag);
            chunk.setTokenCount(frag.split("\\s+").length); // Simple word-based token estimate
            chunk.setOrganizationId(doc.getOrganizationId());

            // Phase 8 additions
            chunk.setEstimatedCost(0.0);
            chunk.setLanguage("en");
            chunk.setPriority(0);
            String sha256 = computeSha256(frag);
            chunk.setChunkHash(sha256);
            chunk.setContentChecksum(sha256);
            chunk.setEmbeddingEligibility(true);
            chunk.setProcessingStatus("PENDING");

            chunkRepository.save(chunk);
        }

        // Save AI index metadata status
        DocumentIndexMetadata meta = metadataRepository.findById(documentId)
                .orElseGet(() -> {
                    DocumentIndexMetadata m = new DocumentIndexMetadata();
                    m.setDocument(doc);
                    return m;
                });

        meta.setChunkCount(fragments.size());
        meta.setEstimatedTokenCount(content.split("\\s+").length);
        meta.setContentHash(String.valueOf(content.hashCode()));
        meta.setEmbeddingStatus("PENDING");
        metadataRepository.save(meta);

        log.info("Finished chunking document. Total chunks recorded: {}", fragments.size());
        eventPublisher.publishEvent(new KnowledgeChunkPreparedEvent(
                doc.getOrganizationId(), documentId, fragments.size(), MdcHelper.getCorrelationId()));
    }

    private String computeSha256(String text) {
        if (text == null) {
            return "";
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found", e);
            return "";
        }
    }
}
