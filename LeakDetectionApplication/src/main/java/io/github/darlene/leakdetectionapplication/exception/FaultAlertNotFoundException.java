package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested FaultAlert cannot be found in the database.
 * Maps to HTTP 404 Not found
 */

@RespomseStatus(HttpStatus.NOT_FOUND)
public class FaultAlertNotFoundException extends RuntimeException{
    public FaultAlertNotFoundException(String message){
        super(message);
    }

    public FaultAlertNotFoundException(String message,Throwable cause){
        super(message, cause);
    }
}