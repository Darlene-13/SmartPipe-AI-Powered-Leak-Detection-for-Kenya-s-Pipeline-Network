package io.github.darlene.leakdetectionapplication.security;

// Spring framework
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

//Lombok
import lombok.extern.slf4j.Slf4j;

// Java standard library
import java.util.Date;
import java.security.Key;
import java.crypto.SecretKey;

//JWT (Json Web Token Library)
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;


/**
 * Provides JWT token generation, validation, and parsing utilities.
 * Used by JwtAuthFilter to validate incoming requests and by
 * AuthService to generate tokens on successful login.
 */

@Slf4j  // This is a universal logger connector, used for logging errors or important info, warnings.
@Component
public class JwtTokenProvider{

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Generates token from the username if we had roles we would have taken the parameter role as well
    public String generateToken(String username){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith((SecretKey) getSigningKey())   // getSigningKey() is our private helpder methods to get the secrete key
                .compact();
    }

    // Validate or verify the token
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayLoad();
             return true;
        } catch (JwtException e){
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    //Method to extract username from token
    public String extractUsername(Sting token){
        return Jwts.parser()
                .verifyWith()
                .build()
                .parseSignedClaims(token)
                .getPayLoad()
                .getSubject();
    }

    //Private helpder that converts secret string to key object
    private Key getSigningKey(){
        byte [] bytes = Decoders.BASE64.decode(secret);
        return keys.hmacsShaKeyFor (keyBytes);
    }
}