package net.techtrends.general.exception;

public class MaxBufferSizeExceededException extends Exception {

    public MaxBufferSizeExceededException() {
        super("Packet rejected: buffer size exceeds maxBufferSize!");
    }
}
