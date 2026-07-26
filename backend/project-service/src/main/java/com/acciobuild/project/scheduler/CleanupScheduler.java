package com.acciobuild.project.scheduler;

import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.repository.ProjectRepository;
import com.acciobuild.project.enums.ProjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Scheduler executing automatic project state maintenance jobs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final ProjectRepository projectRepository;

    /**
     * Daily maintenance run at 1:00 AM to permanently clean up soft-deleted projects
     * after their 30-day retention period.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void executeSoftDeleteCleanup() {
        log.info("Executing Project Service soft-delete retention cleanup job...");
        LocalDateTime retentionThreshold = LocalDateTime.now().minusDays(30);

        try {
            // Find projects soft-deleted before retention limit
            List<Project> toCleanup = projectRepository.findAll().stream()
                    .filter(p -> p.getStatus() == ProjectStatus.DELETED && p.getUpdatedAt().isBefore(retentionThreshold))
                    .toList();

            for (Project project : toCleanup) {
                log.info("Permanently purging project ID: {} (code: {})", project.getId(), project.getProjectCode());
                projectRepository.delete(project);
            }

            log.info("Cleanup Job Complete. Purged {} projects.", toCleanup.size());
        } catch (Exception e) {
            log.error("Failed to run soft-delete cleanup task", e);
        }
    }

    /**
     * Daily maintenance run at 2:00 AM to archive projects that have remained inactive
     * (unmodified) for more than 180 days.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void executeAutoArchival() {
        log.info("Executing Project Service auto-archival job...");
        LocalDateTime inactiveThreshold = LocalDateTime.now().minusDays(180);

        try {
            List<Project> toArchive = projectRepository.findAll().stream()
                    .filter(p -> p.getStatus() == ProjectStatus.ACTIVE && p.getUpdatedAt().isBefore(inactiveThreshold))
                    .toList();

            for (Project project : toArchive) {
                log.info("Auto-archiving inactive project ID: {} (code: {})", project.getId(), project.getProjectCode());
                project.setStatus(ProjectStatus.ARCHIVED);
                projectRepository.save(project);
            }

            log.info("Auto-Archival Job Complete. Archived {} projects.", toArchive.size());
        } catch (Exception e) {
            log.error("Failed to run project archival task", e);
        }
    }
}
