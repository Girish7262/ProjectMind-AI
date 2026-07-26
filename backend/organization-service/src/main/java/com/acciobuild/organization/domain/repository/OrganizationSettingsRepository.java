package com.acciobuild.organization.domain.repository;

import com.acciobuild.organization.domain.model.OrganizationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against OrganizationSettings table.
 */
@Repository
public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettings, UUID> {
}
