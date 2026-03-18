package io.github.darlene.leakdetectionapplication.security;

// Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Web
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Lombok
import lombok.RequiredArgsConstructor;

// Java standard library
import java.util.List;

/**
 * Spring Security configuration for the leak detection API.
 * Configures JWT-based stateless authentication, CORS for the React dashboard,
 * public and protected endpoint rules, and password encoding strategy.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig{

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .crsf(AbstactHttpConfigurer::disable)
                .crsf(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session  ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login"
                        )
                )
    }


}