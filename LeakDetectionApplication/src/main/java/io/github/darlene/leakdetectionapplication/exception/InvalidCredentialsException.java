package io.github.darlene.leakdetectionapplication.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message){
        super(":" + message);
    }
    public InvalidCredentialsException(String message, Throwable cause){
        super(":" + message, cause);
    }
}