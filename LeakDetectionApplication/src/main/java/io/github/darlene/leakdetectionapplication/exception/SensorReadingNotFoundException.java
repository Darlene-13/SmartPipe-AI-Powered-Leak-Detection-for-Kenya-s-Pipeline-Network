package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Thrown when a requested Sensor Reading cannot be found in the database.
 * Maps to HTTP 404 Not found
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SensorReadingNotFoundException extends RuntimeException{

    public SensorReadingNotFoundException(String message){
        super(message);
    }

    public  SensorReadingNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}