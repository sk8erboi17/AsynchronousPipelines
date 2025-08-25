package it.sk8erboi17.network.transformers.encoder.op;

import it.sk8erboi17.network.transformers.encoder.DataEncoder;
import it.sk8erboi17.network.transformers.pool.ByteBuffersPool;
import it.sk8erboi17.exception.MaxBufferSizeExceededException;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.utils.FailWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Handles the logic for building and sending frames.
 * This class is a protocol-level component, focused on data serialization.
 * It uses a DataEncoder instance to perform the actual network I/O.
 */
public class FrameEncoder {
    private static final byte START_MARKER = 0x01;
    private static final Logger log = LoggerFactory.getLogger(FrameEncoder.class);

    private final DataEncoder dataEncoder;
    private final ByteBuffersPool pool;

    public FrameEncoder(DataEncoder dataEncoder) {
        if (dataEncoder == null) {
            throw new IllegalArgumentException("DataEncoder cannot be null.");
        }
        this.dataEncoder = dataEncoder;
        this.pool = ByteBuffersPool.getInstance();
    }

    /**
     * Sends a heartbeat message to the server to keep the connection alive.
     * A heartbeat is a special frame with marker 0x00 and no payload.
     * @param callback The callback to notify upon completion or failure.
     */
    public void sendHeartbeat(Callback callback) {
        byte marker = 0x00;
        int payloadSize = 0;
        Consumer<ByteBuffer> payloadWriter = buffer -> { /* No-op */ };
        buildAndSendFrame(marker, payloadSize, callback, payloadWriter);
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
        int totalPacketSize = 1 + Integer.BYTES + payloadSize;

        ByteBuffer outputBuffer;
        try {
            outputBuffer = pool.acquire(totalPacketSize);
        } catch (InterruptedException | MaxBufferSizeExceededException e) {
            log.error("Failed to acquire buffer to send data. Cause: {}", e.getMessage());
            FailWriter.writeFile("Failed to acquire buffer to send data. Cause: ", e);
            if (callback != null) {
                callback.completeExceptionally(e);
            }
            return;
        }

        if (outputBuffer.capacity() < totalPacketSize) {
            Exception e = new IllegalStateException("Acquired buffer is smaller than required packet size.");
            log.error("Buffer capacity error: required={}, actual={}", totalPacketSize, outputBuffer.capacity(), e);
            FailWriter.writeFile("Buffer capacity error. Cause: ", e);
            DataEncoder.releaseBuffer(pool, outputBuffer);
            if (callback != null) {
                callback.completeExceptionally(e);
            }
        }

        try {
            outputBuffer.put(START_MARKER);
            outputBuffer.putInt(payloadSize + 1);
            outputBuffer.put(dataTypeMarker);
            payloadWriter.accept(outputBuffer);
            outputBuffer.flip();

            // Delega l'invio al DataEncoder, che è il responsabile dell'I/O di rete.
            dataEncoder.send(outputBuffer, callback);
        } catch (Exception e) {
            log.error("Error while assembling the frame", e);
            FailWriter.writeFile("Error while assembling the frame. Cause: ", e);
            DataEncoder.releaseBuffer(pool, outputBuffer);
            if (callback != null) {
                callback.completeExceptionally(e);
            }
        }
    }
}
