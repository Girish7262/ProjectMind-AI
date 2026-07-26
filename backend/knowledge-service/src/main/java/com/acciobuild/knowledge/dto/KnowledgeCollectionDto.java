package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Payload DTO representing a Knowledge Collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeCollectionDto {
    private UUID id;
    private UUID projectId;
    private UUID organizationId;
    private String name;
    private String description;
    private String visibility;
}
