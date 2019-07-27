package com.github.abnair24.exception;

public class CacheLoadingException extends Exception {

    public CacheLoadingException() {
        super();
    }

    public CacheLoadingException(String message) {
        super(message);
    }

    public CacheLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheLoadingException(Throwable cause) {
        super(cause);
    }
}
