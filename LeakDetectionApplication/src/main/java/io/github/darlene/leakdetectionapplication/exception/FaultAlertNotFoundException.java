package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class FaultAlertNotFoundException extends RuntimeException{
    public FaultAlertNotFoundException(String message){
        super(":" + message);
    }

    public FaultAlertNotFoundException(String message,Throwable cause){
        super(":" + message, cause);
    }
}