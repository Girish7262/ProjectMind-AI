package com.acciobuild.organization.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.organization.domain.event.OrganizationCreatedEvent;
import com.acciobuild.organization.domain.event.OrganizationDeletedEvent;
import com.acciobuild.organization.domain.event.OrganizationUpdatedEvent;
import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.domain.model.OrganizationMember;
import com.acciobuild.organization.domain.model.OrganizationSettings;
import com.acciobuild.organization.domain.repository.OrganizationMemberRepository;
import com.acciobuild.organization.domain.repository.OrganizationRepository;
import com.acciobuild.organization.domain.repository.OrganizationSettingsRepository;
import com.acciobuild.organization.dto.OrganizationDto;
import com.acciobuild.organization.dto.OrganizationRequest;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import com.acciobuild.organization.enums.OrganizationStatus;
import com.acciobuild.organization.exception.DuplicateOrganizationException;
import com.acciobuild.organization.exception.OrganizationNotFoundException;
import com.acciobuild.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation managing Organization aggregate rules and life cycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationSettingsRepository settingsRepository;
    private final OrganizationMemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrganizationDto> createOrganization(OrganizationRequest request, UUID creatorUserId) {
        log.info("Attempting to create organization: {}", request.getOrganizationName());

        if (organizationRepository.existsByOrganizationCode(request.getOrganizationCode())) {
            throw new DuplicateOrganizationException("Organization code already exists: " + request.getOrganizationCode());
        }
        if (organizationRepository.existsByOrganizationName(request.getOrganizationName())) {
            throw new DuplicateOrganizationException("Organization name already exists: " + request.getOrganizationName());
        }

        UUID orgId = UUID.randomUUID();
        Organization org = new Organization();
        org.setId(orgId);
        org.setOrganizationCode(request.getOrganizationCode());
        org.setOrganizationName(request.getOrganizationName());
        org.setDisplayName(request.getDisplayName());
        org.setDescription(request.getDescription());
        org.setLogoUrl(request.getLogoUrl());
        org.setWebsite(request.getWebsite());
        org.setIndustry(request.getIndustry());
        org.setOrganizationSize(request.getOrganizationSize());
        org.setCountry(request.getCountry());
        org.setTimezone(request.getTimezone());
        org.setStatus(OrganizationStatus.ACTIVE);
        
        // Audit details
        org.setCreatedBy(creatorUserId);
        org.setUpdatedBy(creatorUserId);

        // Map default Settings
        OrganizationSettings settings = new OrganizationSettings();
        settings.setOrganization(org);
        settings.setAiEnabled(true);
        settings.setKnowledgeSharingEnabled(true);
        settings.setDefaultVisibility("PRIVATE");
        settings.setMaxProjects(10);
        settings.setMaxMembers(50);
        org.setSettings(settings);

        Organization savedOrg = organizationRepository.save(org);

        // Auto enroll creator as OWNER member
        OrganizationMember owner = new OrganizationMember();
        owner.setId(UUID.randomUUID());
        owner.setOrganization(savedOrg);
        owner.setUserId(creatorUserId);
        owner.setRole(MemberRole.OWNER);
        owner.setStatus(MemberStatus.ACTIVE);
        owner.setJoinedAt(LocalDateTime.now());
        memberRepository.save(owner);

        // Publish event
        eventPublisher.publishEvent(new OrganizationCreatedEvent(
                orgId, 
                savedOrg.getOrganizationCode(), 
                savedOrg.getOrganizationName(), 
                creatorUserId, 
                MdcHelper.getCorrelationId()
        ));

        return ApiResponse.<OrganizationDto>builder()
                .status(201)
                .message("Organization created successfully.")
                .data(mapToDto(savedOrg))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrganizationDto> updateOrganization(UUID organizationId, OrganizationRequest request) {
        log.info("Updating organization ID: {}", organizationId);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));

        if (org.isDeleted()) {
            throw new BusinessException("Deleted organizations cannot be modified.", "ORGANIZATION_DELETED");
        }

        // Validate code change collisions
        if (!org.getOrganizationCode().equals(request.getOrganizationCode()) &&
                organizationRepository.existsByOrganizationCode(request.getOrganizationCode())) {
            throw new DuplicateOrganizationException("Organization code already in use.");
        }
        // Validate name change collisions
        if (!org.getOrganizationName().equals(request.getOrganizationName()) &&
                organizationRepository.existsByOrganizationName(request.getOrganizationName())) {
            throw new DuplicateOrganizationException("Organization name already in use.");
        }

        org.setOrganizationCode(request.getOrganizationCode());
        org.setOrganizationName(request.getOrganizationName());
        org.setDisplayName(request.getDisplayName());
        org.setDescription(request.getDescription());
        org.setLogoUrl(request.getLogoUrl());
        org.setWebsite(request.getWebsite());
        org.setIndustry(request.getIndustry());
        org.setOrganizationSize(request.getOrganizationSize());
        org.setCountry(request.getCountry());
        org.setTimezone(request.getTimezone());

        Organization saved = organizationRepository.save(org);

        eventPublisher.publishEvent(new OrganizationUpdatedEvent(
                organizationId, 
                saved.getOrganizationName(), 
                saved.getStatus().name(), 
                MdcHelper.getCorrelationId()
        ));

        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> deleteOrganization(UUID organizationId) {
        log.warn("Soft deleting organization ID: {}", organizationId);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));

        org.setDeleted(true);
        org.setStatus(OrganizationStatus.DEACTIVATED);
        organizationRepository.save(org);

        eventPublisher.publishEvent(new OrganizationDeletedEvent(organizationId, MdcHelper.getCorrelationId()));

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Organization soft deleted successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrganizationDto> getOrganizationById(UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));

        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization details fetched.")
                .data(mapToDto(org))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrganizationDto> getOrganizationByCode(String organizationCode) {
        Organization org = organizationRepository.findByOrganizationCode(organizationCode)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization code not found: " + organizationCode));

        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization details fetched.")
                .data(mapToDto(org))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrganizationDto> activateOrganization(UUID organizationId) {
        log.info("Activating organization ID: {}", organizationId);
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
        org.setStatus(OrganizationStatus.ACTIVE);
        Organization saved = organizationRepository.save(org);
        eventPublisher.publishEvent(new com.acciobuild.organization.domain.event.OrganizationActivatedEvent(organizationId, MdcHelper.getCorrelationId()));
        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization activated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrganizationDto> suspendOrganization(UUID organizationId) {
        log.info("Suspending organization ID: {}", organizationId);
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
        org.setStatus(OrganizationStatus.SUSPENDED);
        Organization saved = organizationRepository.save(org);
        eventPublisher.publishEvent(new com.acciobuild.organization.domain.event.OrganizationSuspendedEvent(organizationId, MdcHelper.getCorrelationId()));
        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization suspended successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrganizationDto> archiveOrganization(UUID organizationId) {
        log.info("Archiving organization ID: {}", organizationId);
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
        org.setStatus(OrganizationStatus.ARCHIVED);
        Organization saved = organizationRepository.save(org);
        eventPublisher.publishEvent(new com.acciobuild.organization.domain.event.OrganizationArchivedEvent(organizationId, MdcHelper.getCorrelationId()));
        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization archived successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrganizationDto> restoreOrganization(UUID organizationId) {
        log.info("Restoring soft-deleted organization ID: {}", organizationId);
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
        org.setDeleted(false);
        org.setStatus(OrganizationStatus.ACTIVE);
        Organization saved = organizationRepository.save(org);
        eventPublisher.publishEvent(new com.acciobuild.organization.domain.event.OrganizationActivatedEvent(organizationId, MdcHelper.getCorrelationId()));
        return ApiResponse.<OrganizationDto>builder()
                .status(200)
                .message("Organization restored successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> permanentDelete(UUID organizationId) {
        log.warn("Permanently deleting organization ID: {}", organizationId);
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
        organizationRepository.delete(org);
        eventPublisher.publishEvent(new com.acciobuild.organization.domain.event.OrganizationDeletedEvent(organizationId, MdcHelper.getCorrelationId()));
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Organization permanently deleted.")
                .build();
    }

    private OrganizationDto mapToDto(Organization org) {
        if (org == null) return null;
        return OrganizationDto.builder()
                .id(org.getId())
                .organizationCode(org.getOrganizationCode())
                .organizationName(org.getOrganizationName())
                .displayName(org.getDisplayName())
                .description(org.getDescription())
                .logoUrl(org.getLogoUrl())
                .website(org.getWebsite())
                .industry(org.getIndustry())
                .organizationSize(org.getOrganizationSize())
                .country(org.getCountry())
                .timezone(org.getTimezone())
                .status(org.getStatus().name())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
