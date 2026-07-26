package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.CitationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping reference citations linked with generated messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitationDto {
    private UUID id;
    private UUID organizationId;
    private UUID messageId;
    private CitationType citationType;
    private UUID sourceId;
    private String title;
    private String url;
    private String snippet;
    private Integer startIndex;
    private Integer endIndex;
    private LocalDateTime createdAt;
}
