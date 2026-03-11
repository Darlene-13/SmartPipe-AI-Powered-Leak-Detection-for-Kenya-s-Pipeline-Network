package io.github.darlene.leakdetectionapplication.exception;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Standardized error response returned by GlobalExceptionHandler.
 * Ensures all API errors follow a consistent JSON structure
 * for predictable error handling on the React dashboard.
 */


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private Integer status;

    private String error;

    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;

}