package com.acciobuild.ai.domain.model;

import com.acciobuild.ai.enums.ProviderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping individual settings of available LLM backend providers.
 */
@Entity
@Table(name = "ai_provider_configurations")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiProviderConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private ProviderType providerType;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "api_key_vault_ref")
    private String apiKeyVaultRef;

    @Column(name = "default_model")
    private String defaultModel;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
