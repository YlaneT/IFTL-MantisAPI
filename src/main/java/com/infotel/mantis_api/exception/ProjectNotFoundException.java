package com.infotel.mantis_api.exception;

public class ProjectNotFoundException extends Exception {
    public ProjectNotFoundException (String message) {
        super(message);
    }
    
    public ProjectNotFoundException (String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProjectNotFoundException (Throwable cause) {
        super(cause);
    }
}
