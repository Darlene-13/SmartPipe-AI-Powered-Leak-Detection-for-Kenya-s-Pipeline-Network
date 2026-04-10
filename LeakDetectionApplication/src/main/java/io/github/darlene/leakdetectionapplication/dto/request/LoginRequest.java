package io.github.darlene.leakdetectionapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 *
 * Request DTO for user authentication
 * Received from the dashboard login form via POST /api/auth/login.
 *
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest{

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

}