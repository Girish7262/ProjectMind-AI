package com.acciobuild.organization.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing OrganizationSettings.
 * Linked to the Organization aggregate via a maps-id one-to-one constraint.
 */
@Entity
@Table(name = "organization_settings")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class OrganizationSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_organization_settings_organization"))
    private Organization organization;

    @Column(name = "ai_enabled", nullable = false)
    private boolean aiEnabled = true;

    @Column(name = "knowledge_sharing_enabled", nullable = false)
    private boolean knowledgeSharingEnabled = true;

    @NotNull(message = "Default visibility configuration is required.")
    @Size(max = 20)
    @Column(name = "default_visibility", nullable = false, length = 20)
    private String defaultVisibility = "PRIVATE";

    @Column(name = "max_projects", nullable = false)
    private int maxProjects = 10;

    @Column(name = "max_members", nullable = false)
    private int maxMembers = 50;

    // Feature Flags
    @Column(name = "knowledge_base_enabled", nullable = false)
    private boolean knowledgeBaseEnabled = true;

    @Column(name = "project_module_enabled", nullable = false)
    private boolean projectModuleEnabled = true;

    @Column(name = "document_upload_enabled", nullable = false)
    private boolean documentUploadEnabled = true;

    @Column(name = "api_access_enabled", nullable = false)
    private boolean apiAccessEnabled = true;

    @Column(name = "team_management_enabled", nullable = false)
    private boolean teamManagementEnabled = true;

    @Column(name = "audit_logs_enabled", nullable = false)
    private boolean auditLogsEnabled = true;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    // Operational Settings
    @Column(name = "max_storage_gb", nullable = false)
    private int maxStorageGb = 10;

    @Column(name = "max_api_requests_per_day", nullable = false)
    private int maxApiRequestsPerDay = 5000;

    @Column(name = "allowed_file_size", nullable = false)
    private int allowedFileSize = 50; // default 50MB

    @Size(max = 200)
    @Column(name = "allowed_file_types", length = 200)
    private String allowedFileTypes = "pdf,docx,png,jpeg,txt";

    @Size(max = 10)
    @Column(name = "default_language", length = 10)
    private String defaultLanguage = "en";

    @Size(max = 100)
    @Column(name = "default_timezone", length = 100)
    private String defaultTimezone = "UTC";
}
