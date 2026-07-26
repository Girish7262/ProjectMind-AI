package com.acciobuild.knowledge.scheduler;

import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled compliance worker archiving inactive files and purging soft-deleted items.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final KnowledgeDocumentRepository documentRepository;

    /**
     * Nightly scheduler task executing auto-archivals on documents inactive for >180 days.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void archiveInactiveDocuments() {
        log.info("CleanupScheduler: Scanning for inactive knowledge files...");
        LocalDateTime threshold = LocalDateTime.now().minusDays(180);
        List<KnowledgeDocument> docs = documentRepository.findAll().stream()
                .filter(d -> d.getStatus() == KnowledgeStatus.PUBLISHED && d.getUpdatedAt().isBefore(threshold))
                .toList();

        for (KnowledgeDocument d : docs) {
            log.info("CleanupScheduler: Auto-archiving document ID: {} due to inactivity.", d.getId());
            d.setStatus(KnowledgeStatus.ARCHIVED);
            d.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(d);
        }
    }

    /**
     * Daily scheduler task permanently purging soft-deleted items older than 30 days.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void purgeDeletedDocuments() {
        log.info("CleanupScheduler: Scanning for soft-deleted items ready for permanent purge...");
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<KnowledgeDocument> docs = documentRepository.findAll().stream()
                .filter(d -> d.getStatus() == KnowledgeStatus.DELETED && d.getUpdatedAt().isBefore(threshold))
                .toList();

        for (KnowledgeDocument d : docs) {
            log.info("CleanupScheduler: Purging document ID: {} permanently.", d.getId());
            documentRepository.delete(d);
        }
    }
}
