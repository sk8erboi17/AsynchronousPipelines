package it.sk8erboi17.exception;

public class MaxBufferSizeExceededException extends Exception {

    public MaxBufferSizeExceededException(String message) {
        super(message);
    }
    public MaxBufferSizeExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
