package net.techtrends.network.pipeline.out;

import net.techtrends.listeners.output.OutputListener;
import net.techtrends.network.pipeline.Pipeline;

import java.nio.channels.AsynchronousSocketChannel;


public class PipelineOut implements Pipeline {
    private final OutputListener outputListener;

    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer) {
        outputListener = new OutputListener(client, initBuffer, allocateDirect);
    }

    public void registerRequest(Request request) {
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

    @Override
    public void closePipeline() {
        outputListener.close();
    }


}
