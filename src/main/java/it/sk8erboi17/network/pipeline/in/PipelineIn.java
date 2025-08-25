package it.sk8erboi17.network.pipeline.in;

import it.sk8erboi17.listeners.input.operations.ListenData;
import it.sk8erboi17.network.transformers.decoder.op.FrameDecoder;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.transformers.decoder.DataDecoder;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineIn class correctly initializes the entire inbound data pipeline
 * for a SINGLE client connection. It wires together the necessary components and
 * starts the asynchronous decoding process.
 * An instance of this class should be created for each new client.
 */
public class PipelineIn {

    /**
     * Constructor to initialize and start the inbound data pipeline for a new client.
     *
     * @param client The newly accepted AsynchronousSocketChannel for the client.
     * @param maxFrameLength The maximum allowed size for a single data frame, to configure the decoder.
     */
    public PipelineIn(AsynchronousSocketChannel client, Callback callback,int maxFrameLength) {
        // --- 1. Create the business logic components ---
        // ListenData contains the logic for what to do with a decoded message.
        ListenData listenDataProcessor = new ListenData();

        // The initial buffer size for the FrameDecoder can be a sensible default.
        // This is for the internal reassembly buffer, NOT the read buffer.
        int initialDecoderBufferSize = ByteBuffersPool.LARGE_SIZE;

        // --- 2. Create the stateful frame decoder ---
        // This object will live for the duration of the connection and manage frame reassembly.
        // These fields are now INSTANCE fields, not static.
        // They belong to a single client's pipeline.
        FrameDecoder frameDecoder = new FrameDecoder(
                initialDecoderBufferSize,
                maxFrameLength,
                listenDataProcessor
        );

        // --- 3. Create the stateless I/O engine ---
        // Pass the channel and the frame decoder it needs to feed.
        DataDecoder dataDecoder = new DataDecoder(client, callback, frameDecoder);

        // --- 4. Start the engine! ---
        // This kicks off the asynchronous read loop, which will now run for the
        // lifetime of the connection.
        dataDecoder.startDecoding();
    }
}