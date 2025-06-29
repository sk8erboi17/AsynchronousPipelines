package it.sk8erboi17.network.transformers;

import it.sk8erboi17.exception.MaxBufferSizeExceededException;
import it.sk8erboi17.listeners.output.AsyncChannelSocket;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool; // Ensure this package is correct
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Handles sending data asynchronously and EFFICIENTLY, without creating temporary buffers.
 * It uses a buffer pool, ensuring that every acquired buffer is correctly released.
 * Each send operation is stateless and thread-safe.
 */
public class DataEncoder {

    private static final byte START_MARKER = 0x01;
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
        this.pool = ByteBuffersPool.getInstance(); // Get the pool instance once
    }


    public void sendInt(int data, Callback callback) {
        byte marker = 0x02;
        buildAndSendFrame(marker, Integer.BYTES, callback, buffer -> buffer.putInt(data));
    }

    public void sendString(String data, Callback callback) {
        byte marker = 0x01;
        byte[] stringBytes = data.getBytes(StandardCharsets.UTF_8);
        int payloadSize = Integer.BYTES + stringBytes.length;

        Consumer<ByteBuffer> payloadWriter = buffer -> {
            buffer.putInt(stringBytes.length);
            buffer.put(stringBytes);
        };

        buildAndSendFrame(marker, payloadSize, callback, payloadWriter);
    }

    public void sendFloat(float data, Callback callback) {
        byte marker = 0x03;
        buildAndSendFrame(marker, Float.BYTES, callback, buffer -> buffer.putFloat(data));
    }

    public void sendDouble(double data, Callback callback) {
        byte marker = 0x04;
        buildAndSendFrame(marker, Double.BYTES, callback, buffer -> buffer.putDouble(data));
    }

    public void sendChar(char data, Callback callback) {
        byte marker = 0x05;
        buildAndSendFrame(marker, Character.BYTES, callback, buffer -> buffer.putChar(data));
    }

    public void sendByteArray(byte[] data, Callback callback) {
        byte marker = 0x06;
        int payloadSize = Integer.BYTES + data.length;

        Consumer<ByteBuffer> payloadWriter = buffer -> {
            buffer.putInt(data.length);
            buffer.put(data);
        };

        buildAndSendFrame(marker, payloadSize, callback, payloadWriter);
    }



    /**
     * The core method. It acquires a buffer, assembles the entire frame by
     * executing the 'payloadWriter', and initiates the asynchronous send.
     *
     * @param dataTypeMarker Marker for the data type.
     * @param payloadSize The size in bytes of the payload only.
     * @param callback The callback to notify of the result.
     * @param payloadWriter The action that writes the payload into the provided buffer.
     */
    private void buildAndSendFrame(byte dataTypeMarker, int payloadSize, Callback callback, Consumer<ByteBuffer> payloadWriter) {
        int frameLength = 1 + payloadSize; // dataTypeMarker + payload
        int totalPacketSize = 1 + Integer.BYTES + frameLength; // START_MARKER + frameLength_int + frame

        ByteBuffer outputBuffer;
        try {
            outputBuffer = pool.acquire(totalPacketSize);
        } catch (InterruptedException | MaxBufferSizeExceededException e) {
            log.error("Failed to acquire buffer to send data. Cause: {}", e.getMessage());
            if (callback != null) {
                callback.completeExceptionally(e);
            }
            return;
        }

        try {
            outputBuffer.put(START_MARKER);
            outputBuffer.putInt(frameLength);
            outputBuffer.put(dataTypeMarker);
            payloadWriter.accept(outputBuffer);
            outputBuffer.flip();

            performSend(outputBuffer, callback);
        } catch (Exception e) {
            log.error("Error while assembling the frame", e);
            releaseBuffer(outputBuffer);
            if (callback != null) {
                callback.completeExceptionally(e);
            }
        }
    }

    /**
     * Initiates the asynchronous write operation by creating a dedicated handler.
     */
    private void performSend(ByteBuffer buffer, Callback callback) {
        if (!socketChannel.isOpen()) {
            log.warn("Attempting to send on a closed channel.");
            releaseBuffer(buffer);

            if (callback != null) {
                callback.completeExceptionally(new ClosedChannelException());
            }
            return;
        }

        WriteContext context = new WriteContext(buffer, callback);
        socketChannel.write(buffer, context, writeCompletionHandler);
    }

    /**
     * Safely releases a buffer back to the pool.
     */
    private void releaseBuffer(ByteBuffer buffer) {
        if (buffer != null) {
            try {
                pool.release(buffer);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while releasing buffer", e);
            }
        }
    }


    /**
     * A small inner record to hold the state of a single write operation.
     */
    private record WriteContext(ByteBuffer buffer, Callback originalCallback) {}

    /**

     * A SINGLE, STATIC, and REUSABLE instance of the CompletionHandler.
     * It is stateless and only operates on the data passed to it in the 'context'.
     * This makes it completely thread-safe.
     */
    private final java.nio.channels.CompletionHandler<Integer, WriteContext> writeCompletionHandler =
            new java.nio.channels.CompletionHandler<>() {

                @Override
                public void completed(Integer bytesWritten, WriteContext context) {
                    if (bytesWritten < 0) {
                        failed(new EOFException("Client closed the connection"), context);
                        return;
                    }

                    if (context.buffer.hasRemaining()) {
                        socketChannel.write(context.buffer, context, this);
                    } else {
                        log.trace("Frame sent successfully.");
                        releaseBuffer(context.buffer);

                        if (context.originalCallback != null) {
                            // CORRECTION: Calling 'complete' with a single null argument, as per the interface definition.
                            context.originalCallback.complete(null);
                        }
                    }
                }

                @Override
                public void failed(Throwable exc, WriteContext context) {
                    log.error("Error during asynchronous data send: {}", exc.getMessage(), exc);
                    releaseBuffer(context.buffer);

                    AsyncChannelSocket.closeChannelSocketChannel(socketChannel);

                    if (context.originalCallback != null) {
                        context.originalCallback.completeExceptionally(exc);
                    }
                }
            };
}