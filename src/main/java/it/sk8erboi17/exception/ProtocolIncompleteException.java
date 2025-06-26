package it.sk8erboi17.exception;

public class ProtocolIncompleteException extends RuntimeException {
    public ProtocolIncompleteException(String message) {
        super(message);
    }
    public ProtocolIncompleteException(String message, Throwable cause) {
        super(message, cause);
    }
}