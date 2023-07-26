package com.infotel.mantis_api.exception;

public class IssueFileNotFound extends Exception {
    public IssueFileNotFound (String message) {
        super(message);
    }
    
    public IssueFileNotFound (String message, Throwable cause) {
        super(message, cause);
    }
    
    public IssueFileNotFound (Throwable cause) {
        super(cause);
    }
}
