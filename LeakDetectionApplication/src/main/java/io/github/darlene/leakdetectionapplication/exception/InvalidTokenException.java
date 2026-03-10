package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidTokenException extends RuntimeException{

    public InvalidTokenException(String message){
        super(":" + message);
    }

    public  InvalidTokenException(String message, Throwable cause){
        super(":" + message, cause);
    }
}