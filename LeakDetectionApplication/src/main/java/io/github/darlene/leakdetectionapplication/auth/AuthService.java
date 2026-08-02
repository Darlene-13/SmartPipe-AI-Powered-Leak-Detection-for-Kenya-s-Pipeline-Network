package io.github.darlene.leakdetectionapplication.auth;

import io.github.darlene.leakdetectionapplication.alert.*;
import io.github.darlene.leakdetectionapplication.analytics.*;
import io.github.darlene.leakdetectionapplication.auth.*;
import io.github.darlene.leakdetectionapplication.configuration.*;
import io.github.darlene.leakdetectionapplication.messaging.*;
import io.github.darlene.leakdetectionapplication.monitoring.*;
import io.github.darlene.leakdetectionapplication.pipeline.*;
import io.github.darlene.leakdetectionapplication.recommendation.*;
import io.github.darlene.leakdetectionapplication.sensor.*;
import io.github.darlene.leakdetectionapplication.simulation.*;
import io.github.darlene.leakdetectionapplication.shared.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.darlene.leakdetectionapplication.auth.UserRepository;
import io.github.darlene.leakdetectionapplication.auth.RefreshTokenRepository;
import io.github.darlene.leakdetectionapplication.auth.JwtTokenProvider;
import io.github.darlene.leakdetectionapplication.auth.User;
import io.github.darlene.leakdetectionapplication.auth.UserRole;
import io.github.darlene.leakdetectionapplication.auth.RefreshToken;
import io.github.darlene.leakdetectionapplication.auth.LoginRequest;
import io.github.darlene.leakdetectionapplication.auth.LoginResponse;
import io.github.darlene.leakdetectionapplication.auth.RegisterRequest;
import io.github.darlene.leakdetectionapplication.auth.InvalidTokenException;
import io.github.darlene.leakdetectionapplication.auth.TokenExpiredException;
import io.github.darlene.leakdetectionapplication.auth.InvalidCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.UUID;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse register(RegisterRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> { throw new InvalidCredentialsException("Username already taken"); });

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(UserRole.ROLE_VIEWER)
                .build();

        userRepository.save(user);

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
                .token(newAccessToken)
                .type("Bearer")
                .expiresIn(LocalDateTime.now().plusHours(24))
                .username(user.getUsername())
                .role(user.getUserRole().name())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    private RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }
}
