package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeRelationship;
import com.acciobuild.knowledge.domain.repository.KnowledgeDocumentRepository;
import com.acciobuild.knowledge.domain.repository.KnowledgeRelationshipRepository;
import com.acciobuild.knowledge.dto.KnowledgeRelationshipDto;
import com.acciobuild.knowledge.enums.RelationshipType;
import com.acciobuild.knowledge.exception.KnowledgeDocumentNotFoundException;
import com.acciobuild.knowledge.exception.KnowledgeRelationshipException;
import com.acciobuild.knowledge.service.KnowledgeRelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Knowledge Relationships.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeRelationshipServiceImpl implements KnowledgeRelationshipService {

    private final KnowledgeRelationshipRepository relationshipRepository;
    private final KnowledgeDocumentRepository documentRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeRelationshipDto> linkDocuments(KnowledgeRelationshipDto dto) {
        if (dto.getSourceDocumentId().equals(dto.getTargetDocumentId())) {
            throw new KnowledgeRelationshipException("A document cannot form a relationship with itself.");
        }

        KnowledgeDocument src = documentRepository.findById(dto.getSourceDocumentId())
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Source document not found."));

        KnowledgeDocument tgt = documentRepository.findById(dto.getTargetDocumentId())
                .orElseThrow(() -> new KnowledgeDocumentNotFoundException("Target document not found."));

        // Prevent circular mapping basic check
        List<KnowledgeRelationship> reverse = relationshipRepository.findBySourceDocumentId(dto.getTargetDocumentId());
        for (KnowledgeRelationship r : reverse) {
            if (r.getTargetDocument().getId().equals(dto.getSourceDocumentId())) {
                throw new KnowledgeRelationshipException("Circular relationship mapping detected between documents.");
            }
        }

        KnowledgeRelationship rel = new KnowledgeRelationship();
        rel.setId(UUID.randomUUID());
        rel.setSourceDocument(src);
        rel.setTargetDocument(tgt);
        rel.setRelationshipType(RelationshipType.valueOf(dto.getRelationshipType().toUpperCase().trim()));
        rel.setStrength(dto.getStrength());

        KnowledgeRelationship saved = relationshipRepository.save(rel);
        return ApiResponse.<KnowledgeRelationshipDto>builder()
                .status(201)
                .message("Relationship linked successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<KnowledgeRelationshipDto>> getRelationships(UUID sourceDocumentId) {
        List<KnowledgeRelationshipDto> list = relationshipRepository.findBySourceDocumentId(sourceDocumentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ApiResponse.<List<KnowledgeRelationshipDto>>builder()
                .status(200)
                .message("Relationships fetched.")
                .data(list)
                .build();
    }

    private KnowledgeRelationshipDto mapToDto(KnowledgeRelationship rel) {
        return KnowledgeRelationshipDto.builder()
                .id(rel.getId())
                .sourceDocumentId(rel.getSourceDocument().getId())
                .targetDocumentId(rel.getTargetDocument().getId())
                .relationshipType(rel.getRelationshipType().name())
                .strength(rel.getStrength())
                .build();
    }
}
