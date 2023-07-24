package com.infotel.mantis_api.exception;

public class CustomFieldNotFoundException extends IssueException {
    public CustomFieldNotFoundException (String message) {
        super(message);
    }
    
    public CustomFieldNotFoundException (String message, Throwable cause) {
        super(message, cause);
    }
    
    public CustomFieldNotFoundException (Throwable cause) {
        super(cause);
    }
}
