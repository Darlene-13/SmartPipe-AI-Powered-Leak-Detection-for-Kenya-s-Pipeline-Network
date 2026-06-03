package io.github.darlene.leakdetectionapplication.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the leak detection REST API.
 * Intercepts all exceptions thrown across all controllers and services.
 * Returns consistent JSON error responses to the dashboard client.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FaultAlertNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFaultAlertNotFound(
            FaultAlertNotFoundException ex,
            HttpServletRequest request){

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(SensorReadingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSensorReadingNotFound(
            SensorReadingNotFoundException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ScenarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScenarioNotFound(
            ScenarioNotFoundException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSensorDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSensorData (
            InvalidSensorDataException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Take the exception
     *     → get the binding result
     *         → get all field errors
     *             → stream through each one
     *                 → format it as "fieldName: message"
     *                     → collect all into one comma separated string
     *                         → pass to buildErrorResponse
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request){

        String messages  = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, messages, request);
    }

    // 401 handlers (Unauthorized requests
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredential(
            InvalidCredentialsException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // 500 handlers (Internal Server  Error)
    @ExceptionHandler(RecommendationServiceException.class)
    public ResponseEntity<ErrorResponse> handleRecommendationService(
            RecommendationServiceException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(
        DatabaseException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // 502 handler (Bad Gateway)
    @ExceptionHandler(MLPredictionFailedException.class)
    public ResponseEntity<ErrorResponse> handleBadGateway(
            MLPredictionFailedException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    // 503 handlers(Service unavailable)
    @ExceptionHandler(MLServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleMLServiceUnavailable(
            MLServiceUnavailableException ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }
    @ExceptionHandler(MqttConnectionException.class)
    public ResponseEntity<ErrorResponse> handleMqttConnection(
            MqttConnectionException ex, HttpServletRequest request){
        return  buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    // 500 fallback - catches ANYTHING not caught
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request){
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // Private handler
    // Helper method to build a standardized error response with the given HTTP status and request path.
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}