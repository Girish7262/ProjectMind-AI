package com.acciobuild.knowledge.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

/**
 * Custom UserDetails implementation holding parsed JWT user and tenant fields.
 */
@Getter
public class KnowledgeUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final UUID organizationId;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs user credentials context.
     */
    public KnowledgeUserDetails(UUID userId, String email, UUID organizationId, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.organizationId = organizationId;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
