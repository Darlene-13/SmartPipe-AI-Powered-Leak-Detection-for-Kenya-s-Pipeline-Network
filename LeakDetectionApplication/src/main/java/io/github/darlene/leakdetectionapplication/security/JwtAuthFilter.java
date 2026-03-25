package io.github.darlene.leakdetectionapplication.security;

// Servlet
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

// Spring security.
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Springframework
import org.springframework.stereotype.Component;

// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Java exception
import java.io.IOException;
import java.util.List;

/**
 *  This file intercepts every HTTP request before it gets to the controllers.
 *  It checks if the request has valid JSON TOKEN
 *  If the token is valid it sets the user in spring security context
 *  It extends the onceperrequest filter to guarantee single execution per request
 */



@Component

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter{

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{

        try{

            String token = extractTokenFromRequest(request);

            if(token != null && jwtTokenProvider.validateToken(token)){

                String username = jwtTokenProvider.extractUsername(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {} ", username);
            }
        } catch (Exception e){
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Private methid to extract JWT token from the authorization header
     * Expects header in the format: "Bearer <token>"
     * It Return null if the heaer is missing or malformed
     */

    private String extractTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

}