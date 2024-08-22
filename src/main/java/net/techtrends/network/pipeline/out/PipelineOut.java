package net.techtrends.network.pipeline.out;

<<<<<<< HEAD
import net.techtrends.listeners.output.AsyncDataSender;
import net.techtrends.network.pipeline.out.content.Request;

import java.net.http.HttpClient;
=======
import net.techtrends.listeners.output.OutputListener;
import net.techtrends.network.pipeline.out.content.Request;

>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineOut class is responsible for managing the output pipeline for sending various types of data to a client using asynchronous operations.
 * It utilizes an AsyncDataSender to handle data transmission based on the type of data requested.
 */
public class PipelineOut {
<<<<<<< HEAD
    private AsynchronousSocketChannel client;

    private boolean allocateDirect;

    private int initBuffer;
=======

    private final AsynchronousSocketChannel client;

    private final boolean allocateDirect;

    private final int initBuffer;
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9

    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer) {
        this.client = client;
        this.allocateDirect = allocateDirect;
        this.initBuffer = initBuffer;
    }
<<<<<<< HEAD


    public void handleRequest(Request request) {
        AsyncDataSender asyncDataSender = new AsyncDataSender(client, initBuffer, allocateDirect);
=======
    public void registerRequest(Request request) {
        handleNonHttpRequest(request);
    }

    private void handleNonHttpRequest(Request request) {
        OutputListener outputListener = new OutputListener(client, initBuffer, allocateDirect);
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        Object message = request.getMessage();
        switch (message) {
            case String s -> asyncDataSender.sendString(s, request.getCallback());
            case Integer i -> asyncDataSender.sendInt(i, request.getCallback());
            case Float v -> asyncDataSender.sendFloat(v, request.getCallback());
            case Double v -> asyncDataSender.sendDouble(v, request.getCallback());
            case Character c -> asyncDataSender.sendChar(c, request.getCallback());
            case byte[] bytes -> asyncDataSender.sendByteArray(bytes, request.getCallback());
            case null, default -> System.err.println("Unsupported message type: " + message.getClass().getSimpleName());
        }
    }

}
