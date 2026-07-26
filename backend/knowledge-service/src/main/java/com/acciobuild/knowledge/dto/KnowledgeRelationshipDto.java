package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Payload DTO representing a Knowledge Relationship.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelationshipDto {
    private UUID id;
    private UUID sourceDocumentId;
    private UUID targetDocumentId;
    private String relationshipType;
    private double strength;
}
