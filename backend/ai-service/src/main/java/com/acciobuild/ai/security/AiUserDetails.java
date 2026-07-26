package com.acciobuild.ai.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

/**
 * Custom UserDetails implementation holding credentials and tenant context for the authenticated user.
 */
public class AiUserDetails implements UserDetails {

    private final UUID userId;
    private final String email;
    private final UUID organizationId;
    private final Collection<? extends GrantedAuthority> authorities;

    public AiUserDetails(UUID userId, String email, UUID organizationId, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.organizationId = organizationId;
        this.authorities = authorities;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getEmail() {
        return email;
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
