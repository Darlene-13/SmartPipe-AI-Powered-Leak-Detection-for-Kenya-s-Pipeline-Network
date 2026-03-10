package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class SensorReadingNotFoundException extends RuntimeException{

    public SensorReadingNotFoundException(String message){
        super(":" + message);
    }

    public  SensorReadingNotFoundException(String message, Throwable cause){
        super(":" + message, cause);
    }
}