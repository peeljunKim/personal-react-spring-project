package org.personal.project.exception;

public class CustomJWTException extends RuntimeException {

    public CustomJWTException(String msg) {
        super(msg);
    }
}
