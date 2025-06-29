package it.sk8erboi17.exception;

public class IllegalSizeException extends RuntimeException {
    public IllegalSizeException(String message) {
        super(message);
    }
    public IllegalSizeException(String message, Throwable cause) {
        super(message, cause);
    }}
