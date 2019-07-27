package com.github.abnair24.exception;

public class JsonFormatException extends Exception {

    public JsonFormatException() {
        super();
    }

    public JsonFormatException(String message) {
        super(message);
    }

    public JsonFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonFormatException(Throwable cause) {
        super(cause);
    }
}
