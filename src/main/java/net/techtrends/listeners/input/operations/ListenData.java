package net.techtrends.listeners.input.operations;

import net.techtrends.listeners.response.Callback;
import net.techtrends.network.AggregateCallback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ListenData {

    public void listen(ByteBuffer buffer, AggregateCallback callback) {
        if (buffer.remaining() <= 0) {
            return;
        }
        byte marker = buffer.get();
        if (marker >= 0x01 && marker <= 0x06) {
            switch (marker) {
                case 0x01 -> handleString(buffer, callback);
                case 0x02 -> handleInt(buffer, callback);
                case 0x03 -> handleFloat(buffer, callback);
                case 0x04 -> handleDouble(buffer, callback);
                case 0x05 -> handleChar(buffer, callback);
                case 0x06 -> handleByteArray(buffer, callback);
            }
        } else {
            try {
                String data = StandardCharsets.UTF_8.decode(buffer).toString();
                System.err.println("Error to string: " + data);
                throw new RuntimeException("Error with buffer, do you have enough space?");
            } catch (ClassCastException e) {
                throw new RuntimeException("Error with buffer, do you have enough space?" + e.getMessage(), e);
            }
        }
    }

    private void handleString(ByteBuffer buffer, AggregateCallback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();

        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }

    }

    private void handleInt(ByteBuffer buffer, AggregateCallback callback) {
        int data = buffer.getInt();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

    private void handleFloat(ByteBuffer buffer, AggregateCallback callback) {
        float data = buffer.getFloat();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

    private void handleDouble(ByteBuffer buffer, AggregateCallback callback) {
        double data = buffer.getDouble();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

    private void handleChar(ByteBuffer buffer, AggregateCallback callback) {
        char data = buffer.getChar();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

    private void handleByteArray(ByteBuffer buffer, AggregateCallback callback) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

}
