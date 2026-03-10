package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class RecommendationServiceException extends RuntimeException{

    public RecommendationServiceException(String message){
        super(":" + message);
    }

    public  RecommendationServiceException(String message, Throwable cause){
        super(":" + message, cause);
    }
}