package com.infotel.mantis_api.exception;

public class AccessDenied extends Exception {
    public AccessDenied(String message) {
        super(message);
    }

    public AccessDenied(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessDenied(Throwable cause) {
        super(cause);
    }
}
