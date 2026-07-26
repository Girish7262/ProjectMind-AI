package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.domain.model.KnowledgeCollection;
import com.acciobuild.knowledge.domain.repository.KnowledgeCollectionRepository;
import com.acciobuild.knowledge.dto.KnowledgeCollectionDto;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import com.acciobuild.knowledge.service.KnowledgeCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Knowledge Collections.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeCollectionServiceImpl implements KnowledgeCollectionService {

    private final KnowledgeCollectionRepository collectionRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeCollectionDto> createCollection(KnowledgeCollectionDto dto) {
        KnowledgeCollection col = new KnowledgeCollection();
        col.setId(UUID.randomUUID());
        col.setProjectId(dto.getProjectId());
        col.setOrganizationId(dto.getOrganizationId() != null ? dto.getOrganizationId() : UUID.randomUUID());
        col.setName(dto.getName());
        col.setDescription(dto.getDescription());
        col.setVisibility(KnowledgeVisibility.valueOf(dto.getVisibility().toUpperCase().trim()));

        KnowledgeCollection saved = collectionRepository.save(col);
        return ApiResponse.<KnowledgeCollectionDto>builder()
                .status(201)
                .message("Collection created.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<KnowledgeCollectionDto>> getCollections(UUID projectId) {
        List<KnowledgeCollectionDto> list = collectionRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ApiResponse.<List<KnowledgeCollectionDto>>builder()
                .status(200)
                .message("Collections fetched.")
                .data(list)
                .build();
    }

    private KnowledgeCollectionDto mapToDto(KnowledgeCollection col) {
        return KnowledgeCollectionDto.builder()
                .id(col.getId())
                .projectId(col.getProjectId())
                .organizationId(col.getOrganizationId())
                .name(col.getName())
                .description(col.getDescription())
                .visibility(col.getVisibility().name())
                .build();
    }
}
