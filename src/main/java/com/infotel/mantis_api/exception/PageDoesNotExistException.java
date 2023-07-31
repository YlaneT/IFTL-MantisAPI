package com.infotel.mantis_api.exception;

public class PageDoesNotExistException extends Throwable {
    public PageDoesNotExistException (String s) {}
    
    public PageDoesNotExistException (String message, Throwable cause) {
        super(message, cause);
    }
    
    public PageDoesNotExistException (Throwable cause) {
        super(cause);
    }
}
