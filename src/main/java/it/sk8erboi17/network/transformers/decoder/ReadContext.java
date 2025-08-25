package it.sk8erboi17.network.transformers.decoder;

import it.sk8erboi17.network.transformers.decoder.op.FrameDecoder;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * Holds the long-lived, stateful components for the entire connection.
 */
public class ReadContext{

    private AsynchronousSocketChannel channel;
    private FrameDecoder frameDecoder;
    private Callback callback;
    private ByteBuffersPool pool;

    public ReadContext(AsynchronousSocketChannel channel, FrameDecoder frameDecoder, Callback callback, ByteBuffersPool pool) {
        this.channel = channel;
        this.frameDecoder = frameDecoder;
        this.pool = pool;
        this.callback = callback;
    }

    public FrameDecoder getFrameDecoder() {
        return frameDecoder;
    }

    public void setFrameDecoder(FrameDecoder frameDecoder) {
        this.frameDecoder = frameDecoder;
    }

    public AsynchronousSocketChannel getChannel() {
        return channel;
    }

    public void setChannel(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    public ByteBuffersPool getPool() {
        return pool;
    }

    public void setPool(ByteBuffersPool pool) {
        this.pool = pool;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}