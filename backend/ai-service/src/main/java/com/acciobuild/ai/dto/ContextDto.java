package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.ContextSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object mapping prompt context parameters and source fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextDto {
    private UUID id;
    private UUID organizationId;
    private UUID conversationId;
    private String queryText;
    private LocalDateTime createdAt;
    private List<SourceDto> sources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDto {
        private UUID id;
        private ContextSourceType sourceType;
        private UUID sourceId;
        private String content;
        private Double score;
    }
}
