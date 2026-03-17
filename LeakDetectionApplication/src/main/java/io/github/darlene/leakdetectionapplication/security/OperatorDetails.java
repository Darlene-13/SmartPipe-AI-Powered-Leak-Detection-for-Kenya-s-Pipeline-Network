package io.github.darlene.leakdetectionapplication.security;

// Spring Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Java standard library
import java.util.Collection;
import java.util.List;

/**
 * Represents an authenticated pipeline operator in the Spring Security context.
 * Implements UserDetails to bridge between our domain and Spring Security's expectations.
 * Used by JwtAuthFilter to set the authenticated user in the security context.
 */


public class OperatorDetails{

    private final String username;
    private final string password;
    private final List<GrantedAuthority> authorities;


    // Constructor
    public OperatoreDetails(String username, String password, String role){
        this.username = username;
        this.password = password;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public Collection<?extends GrantedAuthority> getAuthorites(){
        return authorities;
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return username;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled{
        return true;
    }
}