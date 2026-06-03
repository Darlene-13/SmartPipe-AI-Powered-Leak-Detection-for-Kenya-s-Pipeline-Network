package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSensorDataException extends RuntimeException {
     public InvalidSensorDataException(String message){
         super(message);
     }

     public InvalidSensorDataException(String message, Throwable cause){
         super(message, cause);
     }
}