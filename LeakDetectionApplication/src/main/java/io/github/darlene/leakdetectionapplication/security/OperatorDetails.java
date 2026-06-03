package io.github.darlene.leakdetectionapplication.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import io.github.darlene.leakdetectionapplication.domain.User;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Wraps the User entity and tells Spring Security how to read it.
 * Implements UserDetails so Spring Security can authenticate.
 * Not a Spring bean — created manually by UserDetailsService.
 */
@RequiredArgsConstructor
public class OperatorDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(user.getUserRole().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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

    // Convenience method — gives access to full User entity
    public User getUser() {
        return user;
    }
}
