package it.sk8erboi17.network.pipeline.out;

import it.sk8erboi17.listeners.output.DataEndoder;
import it.sk8erboi17.network.pipeline.out.content.Request;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineOut class is responsible for managing the output pipeline for sending various types of data to a client using asynchronous operations.
 * It utilizes an DataEndoder to handle data transmission based on the type of data requested.
 */
public class PipelineOut {
    private final AsynchronousSocketChannel client;

    private final boolean allocateDirect;

    private final int initBuffer;

    private final boolean performResizing;
    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer, boolean performResizing) {
        this.client = client;
        this.allocateDirect = allocateDirect;
        this.initBuffer = initBuffer;
        this.performResizing = performResizing;
    }


    public void handleRequest(Request request) {
        DataEndoder DataEndoder = new DataEndoder(client, initBuffer, allocateDirect,performResizing);
        Object message = request.getMessage();
        switch (message) {
            case String s -> DataEndoder.sendString(s, request.getCallback());
            case Integer i -> DataEndoder.sendInt(i, request.getCallback());
            case Float v -> DataEndoder.sendFloat(v, request.getCallback());
            case Double v -> DataEndoder.sendDouble(v, request.getCallback());
            case Character c -> DataEndoder.sendChar(c, request.getCallback());
            case byte[] bytes -> DataEndoder.sendByteArray(bytes, request.getCallback());
            case null, default -> System.err.println("Unsupported message type: " + message.getClass().getSimpleName());
        }
    }

}
