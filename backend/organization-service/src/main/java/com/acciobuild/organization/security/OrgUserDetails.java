package com.acciobuild.organization.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

/**
 * Custom UserDetails implementation representing the authenticated user principal.
 * Built statelessly from JWT claims.
 */
@Getter
public class OrgUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final UUID organizationId;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructs the principal wrapper.
     */
    public OrgUserDetails(UUID id, String email, UUID organizationId, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
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
