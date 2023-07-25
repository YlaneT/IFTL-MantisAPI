package com.infotel.mantis_api.exception;

public class FieldNotFoundException extends IssueException {
    public FieldNotFoundException (String message) {
        super(message);
    }
    
    public FieldNotFoundException (String message, Throwable cause) {
        super(message, cause);
    }
    
    public FieldNotFoundException (Throwable cause) {
        super(cause);
    }
}
