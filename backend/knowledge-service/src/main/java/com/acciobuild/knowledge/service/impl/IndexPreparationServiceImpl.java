package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.knowledge.domain.event.KnowledgeIndexedEvent;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeSearchIndex;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeSearchIndexRepository;
import com.acciobuild.knowledge.exception.KnowledgeDocumentNotFoundException;
import com.acciobuild.knowledge.service.IndexPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation executing indexing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IndexPreparationServiceImpl implements IndexPreparationService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeSearchIndexRepository searchIndexRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buildIndex(UUID documentId) {
        log.info("Building full-text search index for document ID: {}", documentId);
        KnowledgeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Document not found."));

        StringBuilder text = new StringBuilder();
        text.append(doc.getTitle()).append(" ");
        if (doc.getSummary() != null) {
            text.append(doc.getSummary()).append(" ");
        }
        if (doc.getMetadata() != null && doc.getMetadata().getKeywords() != null) {
            text.append(doc.getMetadata().getKeywords());
        }

        KnowledgeSearchIndex index = new KnowledgeSearchIndex();
        index.setId(UUID.randomUUID());
        index.setDocument(doc);
        index.setSearchText(text.toString().trim());
        index.setWeight(1.0);
        index.setUpdatedAt(LocalDateTime.now());

        searchIndexRepository.save(index);
        log.info("Full-text search index built successfully.");

        eventPublisher.publishEvent(new KnowledgeIndexedEvent(
                doc.getOrganizationId(), documentId, MdcHelper.getCorrelationId()));
    }
}
