package it.sk8erboi17.exception;

public class MaxBufferSizeExceededException extends Exception {

    public MaxBufferSizeExceededException() {
        super("Packet rejected: buffer size exceeds maxBufferSize!");
    }
}
