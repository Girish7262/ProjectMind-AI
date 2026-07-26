package com.acciobuild.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapped superclass for entities requiring soft delete capability.
 * Keeps track of deletion state, deletion timestamp, and user who performed the deletion.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeleteEntity extends AuditEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    /**
     * Flags the entity as deleted and updates the deletion metadata.
     * @param userId The ID of the user performing the deletion.
     */
    public void softDelete(UUID userId) {
        this.setDeleted(true);
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
    }
}
