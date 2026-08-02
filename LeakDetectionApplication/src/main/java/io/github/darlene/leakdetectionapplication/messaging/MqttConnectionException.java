package io.github.darlene.leakdetectionapplication.messaging;

import io.github.darlene.leakdetectionapplication.alert.*;
import io.github.darlene.leakdetectionapplication.analytics.*;
import io.github.darlene.leakdetectionapplication.auth.*;
import io.github.darlene.leakdetectionapplication.configuration.*;
import io.github.darlene.leakdetectionapplication.messaging.*;
import io.github.darlene.leakdetectionapplication.monitoring.*;
import io.github.darlene.leakdetectionapplication.pipeline.*;
import io.github.darlene.leakdetectionapplication.recommendation.*;
import io.github.darlene.leakdetectionapplication.sensor.*;
import io.github.darlene.leakdetectionapplication.simulation.*;
import io.github.darlene.leakdetectionapplication.shared.*;

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