package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MqttConnectionException extends RuntimeException{

    public MqttConnectionException(String message){
        super(message);
    }

    public  MqttConnectionException(String message, Throwable cause){
        super(message, cause);
    }
}