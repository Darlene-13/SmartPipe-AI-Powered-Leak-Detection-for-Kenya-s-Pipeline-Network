package io.github.darlene.leakdetectionapplication.security;

// Spring framework
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

//Lombok
import lombok.extern.slf4j.Slf4j;

// Java standard library
import java.util.Date;
import java.security.Key;

//JWT (Json Web Token Library)
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Security.Keys;
import io.jsonwebtoken.JwtException;


/**
 *
 */


@Slf4j;b  // This is a universal logger connector, used for logging errors or important info, warnings.
@Component;

public class JwtTokenProvider{

    @Value("{jwt.secret}")
    private String secret;

    @Value("{jwt.expiration-ms}")
    private long expirationMs;

    // Generates token from the username
    public String generateToken(String username){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs)
    }
}