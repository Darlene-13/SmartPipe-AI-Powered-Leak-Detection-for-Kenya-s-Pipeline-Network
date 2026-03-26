package io.github.darlene.leakdetectionapplication.security;

// Springs security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userDetails.UserDetails;

// Our domain files
import package.io.github.darlene.leakdetectionapplication.domain.User;

// Java
import java.util.Collection;
import java.util.List;

// Lombok
import lombok.RequiredArgsConstructor;
/**
 * This file wraps the User entity and tells spring how to read it...
 * It implements user details..
 * This file is not a spring bean thus no component annotation.
 * It gets created manually by the user details services when loading a user
 */
@RequiredArgsConstructor
public class OperatorDetails{

    private final User user;

    // Get authorities.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of( new SimpleGrantedAuthority(user.getUserRole().name()));
    }

    //Get password
    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public String getUsername(){
        return user.getUsername();
    }

    // The four status methods
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
    public boolean isEnabled(){
        return true;
    }

    // Convinience method
    public User getUser(){
        return user;
    }

}