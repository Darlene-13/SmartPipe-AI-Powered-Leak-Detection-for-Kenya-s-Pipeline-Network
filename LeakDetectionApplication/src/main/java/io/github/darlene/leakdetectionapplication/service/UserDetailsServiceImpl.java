package io.github.darlene.leakdetectionapplication.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.github.darlene.leakdetectionapplication.repository.UserRepository;
import io.github.darlene.leakdetectionapplication.security.OperatorDetails;
import io.github.darlene.leakdetectionapplication.domain.User;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads users from the database during authentication.
 * Wraps User entity in OperatorDetails for Spring Security consumption.
 */

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username
                ));
        return new OperatorDetails(user);
    }

}
