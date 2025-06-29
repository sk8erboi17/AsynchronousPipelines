package it.sk8erboi17.exception;

public class MaxFrameLengthExceededException extends RuntimeException {
    public MaxFrameLengthExceededException(String message) {
        super(message);
    }
    public MaxFrameLengthExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
