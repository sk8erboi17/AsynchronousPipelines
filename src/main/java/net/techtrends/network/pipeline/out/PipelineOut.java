package net.techtrends.network.pipeline.out;

import net.techtrends.listeners.output.OutputListener;
import net.techtrends.network.pipeline.out.content.Request;

import java.nio.channels.AsynchronousSocketChannel;

public class PipelineOut {

    private final AsynchronousSocketChannel client;

    private final boolean allocateDirect;

    private final int initBuffer;

    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer) {
        this.client = client;
        this.allocateDirect = allocateDirect;
        this.initBuffer = initBuffer;
    }
    public void registerRequest(Request request) {
        handleNonHttpRequest(request);
    }

    private void handleNonHttpRequest(Request request) {
        OutputListener outputListener = new OutputListener(client, initBuffer, allocateDirect);
        Object message = request.getMessage();
        switch (message) {
            case String s -> outputListener.sendString(s, request.getCallback());
            case Integer i -> outputListener.sendInt(i, request.getCallback());
            case Float v -> outputListener.sendFloat(v, request.getCallback());
            case Double v -> outputListener.sendDouble(v, request.getCallback());
            case Character c -> outputListener.sendChar(c, request.getCallback());
            case byte[] bytes -> outputListener.sendByteArray(bytes, request.getCallback());
            case null, default -> System.err.println("Unsupported message type: " + message.getClass().getSimpleName());
        }
    }

}
