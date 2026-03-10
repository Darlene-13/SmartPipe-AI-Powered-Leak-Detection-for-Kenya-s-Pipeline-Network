package io.github.darlene.leakdetectionapplication.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when credendtial provided are not part of the current database.
 * Maps to HTTP 401 Un authorized
 */

@RespomseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message){
        super(message);
    }
    public InvalidCredentialsException(String message, Throwable cause){
        super(message, cause);
    }
}