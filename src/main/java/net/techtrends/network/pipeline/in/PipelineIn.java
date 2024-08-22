package net.techtrends.network.pipeline.in;

import net.techtrends.BufferBuilder;
<<<<<<< HEAD
import net.techtrends.listeners.input.AsyncDataReceiver;
import net.techtrends.listeners.response.Callback;
=======
import net.techtrends.listeners.input.InputListener;
import net.techtrends.network.AggregateCallback;
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineIn class manages the process of reading data from a client connection using an asynchronous data receiver.
 * It initializes the data receiver with the necessary configurations and starts the reading process using a buffer.
 */
public class PipelineIn {
<<<<<<< HEAD
    private static AsyncDataReceiver asyncDataReceiver;

    // Constructor to initialize and start reading data
    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int bufferSize, Callback callback) {
        asyncDataReceiver = new AsyncDataReceiver(client, bufferSize, callback);
        // Start reading data using a buffer created with BufferBuilder
        asyncDataReceiver.startRead(new BufferBuilder().setInitSize(bufferSize).allocateDirect(allocateDirect).build());
=======

    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int bufferSize, AggregateCallback callback) {
        InputListener inputListener = new InputListener(client, bufferSize, callback);
        inputListener.startRead(new BufferBuilder().setInitSize(bufferSize).allocateDirect(allocateDirect).build());
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
    }

}
