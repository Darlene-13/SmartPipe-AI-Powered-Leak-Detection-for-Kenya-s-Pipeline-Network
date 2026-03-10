package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ScenarioNotFoundException extends RuntimeException{

    public ScenarioNotFoundException(String message){
        super(":" + message);
    }

    public  ScenarioNotFoundException(String message, Throwable cause){
        super(":" + message, cause);
    }
}