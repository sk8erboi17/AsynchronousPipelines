package it.sk8erboi17.network.pipeline.out;

import it.sk8erboi17.listeners.output.DataEncoder;
import it.sk8erboi17.network.pipeline.out.content.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineOut class is responsible for managing the output pipeline for sending various types of data to a client using asynchronous operations.
 * It utilizes an DataEndoder to handle data transmission based on the type of data requested.
 */
public class PipelineOut {
    private static final Logger log = LoggerFactory.getLogger(PipelineOut.class);
    private final boolean allocateDirect;
    private final int initBuffer;
    private final boolean performResizing;
    private AsynchronousSocketChannel client;
    private DataEncoder dataEncoder;

    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer, boolean performResizing) {
        this.client = client;
        this.allocateDirect = allocateDirect;
        this.initBuffer = initBuffer;
        this.performResizing = performResizing;
    }


    public void handleRequest(Request request) {
        dataEncoder = new DataEncoder(client, initBuffer, allocateDirect, performResizing);
        Object message = request.getMessage();
        switch (message) {
            case String s -> dataEncoder.sendString(s, request.getCallback());
            case Integer i -> dataEncoder.sendInt(i, request.getCallback());
            case Float v -> dataEncoder.sendFloat(v, request.getCallback());
            case Double v -> dataEncoder.sendDouble(v, request.getCallback());
            case Character c -> dataEncoder.sendChar(c, request.getCallback());
            case byte[] bytes -> dataEncoder.sendByteArray(bytes, request.getCallback());
            case null, default -> log.error("Unsupported message type: {}", message.getClass().getSimpleName());
        }
    }

    public void setClient(AsynchronousSocketChannel newClient) {
        if (this.client != null && this.client.isOpen() && this.client != newClient) {
            try {
                this.client.close(); // old connection
            } catch (IOException e) {
                log.error("Error with close {}", e.getMessage(), e);
                return;
            }
        }
        this.client = newClient;
        if (this.client != null && this.client.isOpen()) {
            this.dataEncoder = new DataEncoder(this.client, initBuffer, allocateDirect, performResizing); // new encoder
        } else {
            this.dataEncoder = null;
        }
    }


    public AsynchronousSocketChannel getClient() {
        return client;
    }

}
