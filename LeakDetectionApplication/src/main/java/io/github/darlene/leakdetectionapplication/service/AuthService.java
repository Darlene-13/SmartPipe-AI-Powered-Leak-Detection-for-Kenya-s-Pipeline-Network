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
import io.github.darlene.leakdetectionapplication.domain.RefreshToken;
import io.github.darlene.leakdetectionapplication.dto.LoginRequest;
import io.github.darlene.leakdetectionapplication.dto.response.LoginResponse;
import io.github.darlene.leakdetectionapplication.dto.RegisterRequest;
import io.github.darlene.leakdetectionapplication.exception.InvalidTokenException;
import io.github.darlene.leakdetectionapplication.exception.TokenExpiredException;

import java.time.Instant;
import java.util.UUID;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // Register request
    public String register(RegisterRequest request){
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {throw new RuntimeException("Username already taken"); });

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        return "User registered successfully";
    }

    // Login request
    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtTokenProvider.generateToken(user.getUsername());
        RefreshToken refreshToken = generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    // Refresh token request
    public LoginResponse refreshToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found!"));

        if(refreshToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh Token Expired.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername());

        return new LoginResponse(newAccessToken, refreshToken.getToken());
    }

    // Helper to generate refresh token
    private RefreshToken generateRefreshToken(User user){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }
}