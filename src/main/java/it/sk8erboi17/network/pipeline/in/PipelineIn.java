package it.sk8erboi17.network.pipeline.in;

import it.sk8erboi17.BufferBuilder;
import it.sk8erboi17.listeners.input.DataDecoder;
import it.sk8erboi17.listeners.response.Callback;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The PipelineIn class manages the process of reading data from a client connection using an asynchronous data receiver. It initializes the data receiver with the necessary configurations and starts the reading process using a buffer.
 */
public class PipelineIn {
    private static final ExecutorService sharedPool = Executors.newCachedThreadPool();

    // Constructor to initialize and start reading data
    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int frameLength, Callback callback) {
        DataDecoder dataDecoder = new DataDecoder(client, frameLength, callback,sharedPool);
        // Start reading data using a buffer created with BufferBuilder
        dataDecoder.startRead(new BufferBuilder().setInitSize(frameLength).allocateDirect(allocateDirect).build());
    }

}
