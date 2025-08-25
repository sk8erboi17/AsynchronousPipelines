package it.sk8erboi17.network.transformers.encoder;

import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * A record to hold the state of a single write operation.
 * It contains the buffer, the original callback, and references to the
 * necessary components for the completion handler to be fully stateless.
 */
public record WriteContext(ByteBuffer buffer, Callback originalCallback, AsynchronousSocketChannel channel, ByteBuffersPool pool) {

}
