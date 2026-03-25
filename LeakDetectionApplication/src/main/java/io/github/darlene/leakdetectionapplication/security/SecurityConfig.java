package io.github.darlene.leakdetectionapplication.security;

//Spring security
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;


// Spring beans
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

/**
 * This is the rulebook of our application.
 * It defines the endpoints that are public and those that are protected.
 * It configures the password encoding, CORS and session management.
 */

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity

public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .crsf(AbstractHttpConfigurer::disable)
                .crsf(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.CreationPolicy(sessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "v3/api/-docs/**").permitAll()
                        .requestMatchers("/api/sensors/**", "/api/simulate/**").hasRole("OPERATOR")
                        .requestMatchers("/api/alerts/**". "/api/status/**", "/api/analytics/**").hasRole("OPERATOR", "VIEWER")
                        .requestMatchers("/ws/**").authenticated()
                        .anyRequest().authenticated()
                )

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return BCryptPasswordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

}