package io.github.darlene.leakdetectionapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user login
 * Returned by POST /api/auth/login
 * Contains JWT token and expiration information after a successful login
 * Used by the frontend to authorize API requests.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT token used for authentication in subsequent requests. */
    private String token;

    /** Type of the token (usually "Bearer"). */
    private String type;

    /** Expiration date and time of the JWT token. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresIn;

    /** Username of the logged-in user. */
    private String username;

    private String refreshToken;
}