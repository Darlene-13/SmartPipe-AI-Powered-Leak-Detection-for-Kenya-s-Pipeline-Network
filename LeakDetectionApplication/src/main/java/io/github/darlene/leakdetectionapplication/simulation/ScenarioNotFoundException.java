package io.github.darlene.leakdetectionapplication.simulation;

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

/**
 * Thrown when a requested Scenario cannot be found in the database.
 * Maps to HTTP 404 Not found
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ScenarioNotFoundException extends RuntimeException{

    public ScenarioNotFoundException(String message){
        super(message);
    }

    public  ScenarioNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}