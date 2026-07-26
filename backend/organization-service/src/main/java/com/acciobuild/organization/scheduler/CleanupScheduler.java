package com.acciobuild.organization.scheduler;

import com.acciobuild.organization.domain.repository.OrganizationInvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Spring Scheduler component managing daily database maintenance, invitation cleanup,
 * and compliance reporting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final OrganizationInvitationRepository invitationRepository;

    /**
     * Runs daily at midnight to purge expired invitations and generate compliance statistics.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void executeDailyCleanup() {
        log.info("Executing daily database maintenance task...");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Delete unaccepted invitations that have passed their expiration timestamp
            int deletedCount = invitationRepository.deleteExpiredInvitations(now);
            
            log.info("Daily Maintenance Complete: Purged {} expired invitations.", deletedCount);
            generateDailyCleanupReport(deletedCount);
            
        } catch (Exception e) {
            log.error("Maintenance task execution failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Formulates execution reports logs.
     */
    private void generateDailyCleanupReport(int deletedCount) {
        log.info("=== DAILY COMPLIANCE REPORT ===");
        log.info("Timestamp: {}", LocalDateTime.now());
        log.info("Expired Invitations Cleaned: {}", deletedCount);
        log.info("=================================");
    }
}
