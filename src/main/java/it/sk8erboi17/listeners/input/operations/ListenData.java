package it.sk8erboi17.listeners.input.operations;

import it.sk8erboi17.exception.ProtocolIncompleteException;
import it.sk8erboi17.exception.ProtocolViolationException;
import it.sk8erboi17.listeners.response.Callback;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ListenData {

    private static final int INT_BYTES = Integer.BYTES;
    private static final int FLOAT_BYTES = Float.BYTES;
    private static final int DOUBLE_BYTES = Double.BYTES;
    private static final int CHAR_BYTES = Character.BYTES;

    public void listen(byte marker, ByteBuffer buffer, Callback callback) {
        if (buffer.remaining() == 0) {
            callback.completeExceptionally(new IllegalArgumentException("The provided buffer is empty. Cannot process data."));
            return;
        }

        try {
            switch (marker) {
                case 0x01 -> handleString(buffer, callback);
                case 0x02 -> handleInt(buffer, callback);
                case 0x03 -> handleFloat(buffer, callback);
                case 0x04 -> handleDouble(buffer, callback);
                case 0x05 -> handleChar(buffer, callback);
                case 0x06 -> handleByteArray(buffer, callback);
                default -> callback.completeExceptionally(new ProtocolViolationException("Unknown marker received: 0x" + String.format("%02X", marker) + ". Remaining buffer: " + buffer.remaining() + " bytes."));
            }
        } catch (BufferUnderflowException e) {
            callback.completeExceptionally(new ProtocolIncompleteException("Insufficient data in the buffer for the data type expected by marker 0x" + String.format("%02X", marker) + ". Incomplete or malformed message.", e));
        } catch (Exception e) {
            callback.completeExceptionally(new RuntimeException("Unexpected error while processing data with marker 0x" + String.format("%02X", marker) + ": " + e.getMessage(), e));
        }
    }

    private void handleString(ByteBuffer buffer, Callback callback) {
        int length = buffer.getInt();
        if (length < 0 || length > buffer.remaining()) {
            throw new ProtocolViolationException("Invalid length " + length + " (remaining length: " + buffer.remaining() + ")");
        }

        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);
        String data = new String(stringBytes, StandardCharsets.UTF_8);
        callback.complete(data);
    }

    private void handleInt(ByteBuffer buffer, Callback callback) {
        if (buffer.remaining() < INT_BYTES) {
            throw new BufferUnderflowException();
        }
        int data = buffer.getInt();
        callback.complete(data);
    }

    private void handleFloat(ByteBuffer buffer, Callback callback) {
        if (buffer.remaining() < FLOAT_BYTES) {
            throw new BufferUnderflowException();
        }
        float data = buffer.getFloat();
        callback.complete(data);
    }

    private void handleDouble(ByteBuffer buffer, Callback callback) {
        if (buffer.remaining() < DOUBLE_BYTES) {
            throw new BufferUnderflowException();
        }
        double data = buffer.getDouble();
        callback.complete(data);
    }

    private void handleChar(ByteBuffer buffer, Callback callback) {
        if (buffer.remaining() < CHAR_BYTES) {
            throw new BufferUnderflowException();
        }
        char data = buffer.getChar();
        callback.complete(data);
    }

    private void handleByteArray(ByteBuffer buffer, Callback callback) {
        int length = buffer.getInt();
        if (length < 0 || length > buffer.remaining()) {
            throw new ProtocolViolationException("Invalid length " + length + " (remaining length: " + buffer.remaining() + ")");
        }

        byte[] data = new byte[length];
        buffer.get(data);
        callback.complete(data);
    }
}