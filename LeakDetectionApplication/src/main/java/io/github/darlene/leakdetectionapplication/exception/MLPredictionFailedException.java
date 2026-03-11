package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpResponse.BAD_GATEWAY)
public class MLPredictionFailedException extends RuntimeException{

    public MLPredictionFailedException(String message){
        super(message);
    }

    public  MLPredictionFailedException(String message, Throwable cause){
        super(message, cause);
    }
}