package it.sk8erboi17.network.transformers.decoder;

import it.sk8erboi17.exception.MaxBufferSizeExceededException;
import it.sk8erboi17.network.transformers.decoder.op.FrameDecoder;
import it.sk8erboi17.listeners.output.AsyncChannelSocket;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool; // Make sure this package is correct
import it.sk8erboi17.utils.FailWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Acts as a stateless I/O engine that feeds a stateful SocketFrameDecoder.
 * It runs an asynchronous read loop, acquiring a temporary buffer from a pool for each read,
 * passing it to the frame decoder, and ensuring the buffer is always released.
 */
public class DataDecoder {

    private static final Logger log = LoggerFactory.getLogger(DataDecoder.class);

    private final AsynchronousSocketChannel socketChannel;
    private final FrameDecoder frameDecoder;
    private final ByteBuffersPool pool;
    private final Callback callback;

    public DataDecoder(AsynchronousSocketChannel socketChannel,Callback callback, FrameDecoder frameDecoder) {
        if (socketChannel == null || frameDecoder == null) {
            throw new IllegalArgumentException("Channel and FrameDecoder cannot be null.");
        }
        this.socketChannel = socketChannel;
        this.frameDecoder = frameDecoder;
        this.callback = callback;
        this.pool = ByteBuffersPool.getInstance();
    }

    /**
     * Starts the asynchronous read loop.
     * This method initiates the first read, and the loop sustains itself
     * within the CompletionHandler.
     */
    public void startDecoding() {
        // The context holds the stateful components needed by the handler.
        ReadContext context = new ReadContext(socketChannel, frameDecoder,callback, pool);
        armNextRead(context);
    }

    /**
     * Acquires a new buffer and schedules the next asynchronous read.
     */
    private static void armNextRead(ReadContext context) {
        ByteBuffer tempReadBuffer;
        try {
            // Acquire a fresh, temporary buffer for this specific read operation.
            tempReadBuffer = context.getPool().acquire(ByteBuffersPool.LARGE_SIZE);
        } catch (InterruptedException | MaxBufferSizeExceededException e) {
            log.error("Failed to acquire buffer for read operation. Closing connection for {}.", getRemoteAddressSafe(context.getChannel()), e);
            FailWriter.writeFile("Failed to acquire buffer for read operation. Closing connection for " +getRemoteAddressSafe(context.getChannel()), e);
            AsyncChannelSocket.closeChannelSocketChannel(context.getChannel());
            // No buffer was acquired, so nothing to release.
            return;
        }

        ReadOperationContext operationContext = new ReadOperationContext(context, tempReadBuffer);
        context.getChannel().read(
                tempReadBuffer,
                15, TimeUnit.SECONDS, // if client send no hearthbeat is dead
                operationContext,
                readCompletionHandler
        );
    }

    private static String getRemoteAddressSafe(AsynchronousSocketChannel channel) {
        try {
            return channel.getRemoteAddress().toString();
        } catch (IOException e) {
            return "Unknown Client";
        }
    }

    /**
     * Holds the context for a single read operation, including the temporary buffer.
     */
    private record ReadOperationContext(ReadContext parentContext, ByteBuffer buffer) {}

    /**
     * A SINGLE, STATIC, and REUSABLE CompletionHandler for reading. It's stateless.
     */
    private static final CompletionHandler<Integer, ReadOperationContext> readCompletionHandler =
            new CompletionHandler<>() {
                @Override
                public void completed(Integer bytesRead, ReadOperationContext opContext) {
                    ReadContext context = opContext.parentContext;
                    ByteBuffer buffer = opContext.buffer;

                     if (bytesRead == -1) {
                        log.info("Client {} disconnected.", getRemoteAddressSafe(context.getChannel()));
                        context.getFrameDecoder().onClientDisconnected(context.getChannel());
                        releaseBuffer(context.getPool(), buffer);
                        AsyncChannelSocket.closeChannelSocketChannel(context.getChannel());
                        return;
                    }


                    try {
                        // Pass the newly read data to the stateful frame decoder.
                        context.getFrameDecoder().decode(context.getChannel(), buffer, opContext.parentContext().getCallback());
                    } catch (Exception e) {
                        FailWriter.writeFile("Frame decoder threw an exception for client " + getRemoteAddressSafe(context.getChannel()) + ". Closing connection.", e);
                        failed(e, opContext);
                        return;
                    } finally {
                        releaseBuffer(context.getPool(), buffer);
                    }

                    // Re-arm the loop for the next read, which will acquire a new buffer.
                    armNextRead(context);
                }

                @Override
                public void failed(Throwable exc, ReadOperationContext opContext) {
                    ReadContext context = opContext.parentContext;

                    if (exc instanceof InterruptedByTimeoutException) {
                        log.warn("Timeout: Client {} inactive.", getRemoteAddressSafe(context.getChannel()));
                    } else if (exc instanceof AsynchronousCloseException) {                        log.info("Connection to {} was closed during read.", getRemoteAddressSafe(context.getChannel()));
                        FailWriter.writeFile("Connection to "+ getRemoteAddressSafe(context.getChannel()) +" was closed during read.", exc);
                    } else {
                        log.error("Read operation failed for client {}: {}", getRemoteAddressSafe(context.getChannel()), exc.getMessage(), exc);
                        FailWriter.writeFile("Read operation failed for client "+ getRemoteAddressSafe(context.getChannel()), exc);
                    }

                    releaseBuffer(context.getPool(), opContext.buffer);
                    context.getFrameDecoder().onClientDisconnected(context.getChannel());
                    AsyncChannelSocket.closeChannelSocketChannel(context.getChannel());
                }

                private static void releaseBuffer(ByteBuffersPool pool, ByteBuffer buffer) {
                    if (pool != null && buffer != null) {
                        try {
                            pool.release(buffer);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("Thread interrupted while releasing buffer", e);
                            FailWriter.writeFile("Thread interrupted while releasing buffer", e);
                        }
                    }
                }
            };
}