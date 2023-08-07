package com.infotel.mantis_api.exception;

public class AmbiguousProjectException extends Exception {
    public AmbiguousProjectException (String message) {}
    
    public AmbiguousProjectException (String message, Throwable cause) {
        super(message, cause);
    }
    
    public AmbiguousProjectException (Throwable cause) {
        super(cause);
    }
}
