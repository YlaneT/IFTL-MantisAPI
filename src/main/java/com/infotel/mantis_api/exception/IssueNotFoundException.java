package com.infotel.mantis_api.exception;

public class IssueNotFoundException extends IssueException {
    public IssueNotFoundException (String message) {
        super(message);
    }
    
    public IssueNotFoundException (String message, Throwable cause) {
        super(message, cause);
    }
    
    public IssueNotFoundException (Throwable cause) {
        super(cause);
    }
}
