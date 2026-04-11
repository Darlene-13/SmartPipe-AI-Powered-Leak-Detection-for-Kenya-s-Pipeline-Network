package io.github.darlene.leakdetectionapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.darlene.leakdetectionapplication.repository.UserRepository;
import io.github.darlene.leakdetectionapplication.repository.RefreshTokenRepository;
import io.github.darlene.leakdetectionapplication.security.JwtTokenProvider;
import io.github.darlene.leakdetectionapplication.domain.User;
import io.github.darlene.leakdetectionapplication.domain.UserRole;
import io.github.darlene.leakdetectionapplication.domain.RefreshToken;
import io.github.darlene.leakdetectionapplication.dto.request.LoginRequest;
import io.github.darlene.leakdetectionapplication.dto.response.LoginResponse;
import io.github.darlene.leakdetectionapplication.dto.request.RegisterRequest;
import io.github.darlene.leakdetectionapplication.exception.InvalidTokenException;
import io.github.darlene.leakdetectionapplication.exception.TokenExpiredException;
import io.github.darlene.leakdetectionapplication.exception.InvalidCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.UUID;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;

/**
 * Service handling user authentication operations.
 * Manages registration, login, token refresh, and logout.
 * Integrates with Spring Security and JWT token infrastructure.
 */

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // Register request — auto-assigns VIEWER role, auto-logs in after registration
    public LoginResponse register(RegisterRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> { throw new InvalidCredentialsException("Username already taken"); });

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(UserRole.ROLE_VIEWER) // always hardcoded — never trust client-supplied role
                .build();

        userRepository.save(user);

        // Auto-login: generate tokens immediately after registration
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getUserRole());
        RefreshToken refreshToken = generateRefreshToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .type("Bearer")
                .expiresIn(LocalDateTime.now().plusHours(24))
                .username(user.getUsername())
                .role(user.getUserRole().name())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    // Login request
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getUserRole());
        RefreshToken refreshToken = generateRefreshToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .type("Bearer")
                .expiresIn(LocalDateTime.now().plusHours(24))
                .username(user.getUsername())
                .role(user.getUserRole().name())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    // Refresh token request
    public LoginResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found!"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh Token Expired.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getUserRole()
        );

        return LoginResponse.builder()
                .token(accessToken)
                .type("Bearer")
                .expiresIn(LocalDateTime.now().plusHours(24))
                .username(user.getUsername())
                .role(user.getUserRole().name())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    // Logout
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    // Helper to generate refresh token
    private RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }
}