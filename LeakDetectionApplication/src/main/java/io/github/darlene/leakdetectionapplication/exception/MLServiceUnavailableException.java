package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MLServiceUnavailableException extends RuntimeException{

    public MLServiceUnavailableException(String message){
        super(message);
    }

    public  MLServiceUnavailableException(String message, Throwable cause){
        super(message, cause);
    }
}