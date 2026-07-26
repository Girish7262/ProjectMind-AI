package com.acciobuild.auth.security;

import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Spring Security UserDetails implementation wrapping User JPA entity.
 * Maps roles and fine-grained permissions to GrantedAuthorities.
 */
public class AuthUserDetails implements UserDetails {

    private final User user;

    public AuthUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        user.getRoles().forEach(role -> {
            // Add Role Authority (e.g. ROLE_ADMIN)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            
            // Add Permission Authorities (e.g. READ, WRITE)
            role.getPermissions().forEach(permission -> 
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // Use email as username identifier
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // AccioBuild sessions expired tokens handled separately
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Password history checks handled in policy service
    }

    @Override
    public boolean isEnabled() {
        return AccountStatus.ACTIVE.equals(user.getStatus());
    }

    public User getUser() {
        return this.user;
    }
}
