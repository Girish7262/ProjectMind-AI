package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.DocumentIndexMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data Repository for DocumentIndexMetadata.
 */
@Repository
public interface DocumentIndexMetadataRepository extends JpaRepository<DocumentIndexMetadata, UUID> {
}
