package it.sk8erboi17.network.transformers.encoder;

import it.sk8erboi17.listeners.output.AsyncChannelSocket;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool;
import it.sk8erboi17.utils.FailWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;

/**
 * Handles asynchronous writing of ByteBuffers to a socket channel.
 * This class is a network-level component, focused solely on I/O.
 * It manages the buffer pool and a stateless CompletionHandler for writing.
 */
public class DataEncoder {
    private static final Logger log = LoggerFactory.getLogger(DataEncoder.class);

    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffersPool pool;

    /**
     * Constructs a DataEncoder for a specific socket channel.
     * @param socketChannel The non-null channel to write data to.
     */
    public DataEncoder(AsynchronousSocketChannel socketChannel) {
        if (socketChannel == null) {
            throw new IllegalArgumentException("SocketChannel cannot be null.");
        }
        this.socketChannel = socketChannel;
        this.pool = ByteBuffersPool.getInstance();
    }

    /**
     * The core method to send a ByteBuffer. It handles buffer management
     * and asynchronous I/O, without knowing the data's content or protocol.
     *
     * @param buffer The ByteBuffer to send.
     * @param callback The callback to notify upon completion or failure.
     */
    public void send(ByteBuffer buffer, Callback callback) {
        if (!socketChannel.isOpen()) {
            log.warn("Attempting to send on a closed channel.");
            releaseBuffer(pool, buffer);
            if (callback != null) {
                callback.completeExceptionally(new ClosedChannelException());
            }
            return;
        }

        WriteContext context = new WriteContext(buffer, callback, socketChannel, pool);
        socketChannel.write(buffer, context, writeCompletionHandler);
    }

    /**
     * Safely releases a buffer back to the pool. This is a static helper method
     * to make the CompletionHandler's logic simpler and more robust.
     */
    public static void releaseBuffer(ByteBuffersPool pool, ByteBuffer buffer) {
        if (buffer != null) {
            try {
                pool.release(buffer);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while releasing buffer", e);
                FailWriter.writeFile("Thread interrupted while releasing buffer", e);
            }
        }
    }

    /**
     * A SINGLE, STATIC, and REUSABLE instance of the CompletionHandler.
     * It is stateless and only operates on the data passed to it in the 'context'.
     */
    private static final CompletionHandler<Integer, WriteContext> writeCompletionHandler =
            new CompletionHandler<>() {

                @Override
                public void completed(Integer bytesWritten, WriteContext context) {
                    if (bytesWritten < 0) {
                        failed(new EOFException("Client closed the connection"), context);
                        return;
                    }

                    if (context.buffer().hasRemaining()) {
                        // Continue writing the remaining data
                        context.channel().write(context.buffer(), context, this);
                    } else {
                        log.trace("Frame sent successfully.");
                        DataEncoder.releaseBuffer(context.pool(), context.buffer());

                        if (context.originalCallback() != null) {
                            context.originalCallback().complete(null);
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, WriteContext context) {
                    log.error("Error during asynchronous data send: {}", exc.getMessage(), exc);
                    FailWriter.writeFile("Error during asynchronous data send: ", exc);
                    DataEncoder.releaseBuffer(context.pool(), context.buffer());
                    AsyncChannelSocket.closeChannelSocketChannel(context.channel());

                    if (context.originalCallback() != null) {
                        context.originalCallback().completeExceptionally(exc);
                    }
                }
            };
}
