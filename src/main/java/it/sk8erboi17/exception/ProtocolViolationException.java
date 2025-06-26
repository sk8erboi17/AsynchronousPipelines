package it.sk8erboi17.exception;

public class ProtocolViolationException extends RuntimeException {
    public ProtocolViolationException(String message) {
        super(message);
    }
    public ProtocolViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}