package com.infotel.mantis_api.exception;

public class IssueException extends Exception {
    public IssueException (String message) {
        super(message);
    }

    public IssueException (String message, Throwable cause) {
        super(message, cause);
    }

    public IssueException (Throwable cause) {
        super(cause);
    }
}
