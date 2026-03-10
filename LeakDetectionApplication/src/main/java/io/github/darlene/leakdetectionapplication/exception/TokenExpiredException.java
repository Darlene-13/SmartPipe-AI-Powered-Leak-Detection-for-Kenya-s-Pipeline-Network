package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TokenExpiredException extends RuntimeException{

    public TokenExpiredException(String message){
        super(":" + message);
    }

    public  TokenExpiredException(String message, Throwable cause){
        super(":" + message, cause);
    }
}