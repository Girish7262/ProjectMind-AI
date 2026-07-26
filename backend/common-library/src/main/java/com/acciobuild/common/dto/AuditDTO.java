package com.acciobuild.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data transfer object representing standard audit details of resources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    private boolean deleted;
    private Long version;
}
