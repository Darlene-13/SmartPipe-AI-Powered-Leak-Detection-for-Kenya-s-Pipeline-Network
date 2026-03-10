package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpResponse.BAD_GATEWAY)
public class MLPredictionFailed extends RuntimeException{

    public MLPredictionFailed(String message){
        super(message);
    }

    public  MLPredictionFailed(String message, Throwable cause){
        super(message, cause);
    }
}