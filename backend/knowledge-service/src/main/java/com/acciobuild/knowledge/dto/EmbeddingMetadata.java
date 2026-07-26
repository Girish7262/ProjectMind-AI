package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object capturing vector parameters and metadata fields returned by downstream vector processes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID vectorId;
    private int dimension;
    private String checksum;
    private LocalDateTime generatedAt;
}
