package io.github.darlene.leakdetectionapplication.security;

// JWT Library
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;

// Spring library
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; // To make it a spring bean.

// Java library
import java.util.Date;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

// Lombok imports
import lombok.extern.slf4j.Slf4j;

/**
 *  This file provides JWT token generation, validation and parsing utilities.
 *  JWT has the header, payload and signature in it.
 */


@Slf4j
@Component

public class JwtTokenProvider{

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Method to generate token from the username
    public String generateToken(String username, UserRole role){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expierationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .claim("role", role.name())
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

    }

    // Method to validate token
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims()
                    .getPayLoad();
            return true;
        } catch(JwtException e){
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Method to getUsernamefrom token
    public String extractUsername(String token){
        return Jwts.parser()
                .verifyWith()
                .build()
                .parseSignedClaims()
                .getPayload()
                .getSubject();
    }

    // Get role from token method
    public String getRoleFromToken(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // Private helper methid to convert secret key string to key Object
    private Key getSigningKey(){
        byte [] bytes = Decoders.BASE64.decode(secret);
        return Keys.hmacsShaKeyFor (keyBytes);
    }

}