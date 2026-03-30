package io.github.darlene.leakdetectionapplication.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Thrown when a requested user witha certains username cannot be fouynd in the db.
 * Maps to HTTP 404 Not found
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsernameNotFoundException extends RuntimeException{

    public UsernameNotFoundException(String message){
        super(message);
    }

    public  UsernameNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}