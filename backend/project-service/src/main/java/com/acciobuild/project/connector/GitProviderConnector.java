package com.acciobuild.project.connector;

import com.acciobuild.project.domain.model.GitRepository;
import com.acciobuild.project.enums.GitProvider;

/**
 * Strategy interface to be implemented by provider-specific connectors (GitHub, GitLab, etc.).
 */
public interface GitProviderConnector {

    /**
     * Checks if this connector strategy supports the target Git provider.
     */
    boolean supports(GitProvider provider);

    /**
     * Validates connection credentials against the remote Git API.
     */
    void validateConnection(String url, String token);

    /**
     * Synchronizes metadata (commits, branches, changesets) from the remote repository.
     */
    void syncMetadata(GitRepository repository);
}
