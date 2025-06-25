package net.techtrends.network.pipeline.in;

import net.techtrends.BufferBuilder;
import net.techtrends.listeners.input.DataDecoder;
import net.techtrends.listeners.response.Callback;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineIn class manages the process of reading data from a client connection using an asynchronous data receiver. It initializes the data receiver with the necessary configurations and starts the reading process using a buffer.
 */
public class PipelineIn {
    private static DataDecoder DataDecoder;

    // Constructor to initialize and start reading data
    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int bufferSize, Callback callback) {
        DataDecoder = new DataDecoder(client, bufferSize, callback);
        // Start reading data using a buffer created with BufferBuilder
        DataDecoder.startRead(new BufferBuilder().setInitSize(bufferSize).allocateDirect(allocateDirect).build());
    }

}
