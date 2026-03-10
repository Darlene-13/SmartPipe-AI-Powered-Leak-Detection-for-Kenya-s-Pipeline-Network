package io.github.darlene.leakdetectionapplication.exception;

// Imports
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;




public class DatabaseException extends RuntimeException{

    public DatabaseException(String message){
        super(":" + message );
    }

    public DatabaseException(String message, Throwable cause){
        super(""+ message, cause);
    }

}