package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidSensorDataException extends RuntimeException {
     public InvalidSensorDataException(String message){
         super("Invalid Sensor Data: " + message);
     }

     public InvalidSensorDataException(String message, Throwable cause){
         super("Invalid Sensor Data:"+ message, cause);
     }
}