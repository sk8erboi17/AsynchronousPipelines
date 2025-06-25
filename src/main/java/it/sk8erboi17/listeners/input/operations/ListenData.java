package it.sk8erboi17.listeners.input.operations;

import it.sk8erboi17.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The ListenData class is responsible for processing data read from a ByteBuffer.
 * It identifies the type of data based on an initial marker byte and then decodes the data accordingly.
 * The class uses a Callback to handle the processed data or report errors.
 */
public class ListenData {


    public void listen(byte marker,ByteBuffer buffer, Callback callback) {
        // If the buffer has no remaining bytes, return immediately
        if (buffer.remaining() <= 0) {
            return;
        }


        // Depending on the marker, process the buffer accordingly
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
            // If the marker is out of the expected range, attempt to decode the buffer as a string and log an error
            try {
                throw new RuntimeException("Error with buffer, do you have enough space?");
            } catch (ClassCastException e) {
                throw new RuntimeException("Error with buffer, do you have enough space?" + e.getMessage(), e);
            }
        }
    }

    // Method to handle String data, decode it from the buffer and pass it to the callback
    private void handleString(ByteBuffer buffer, Callback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        callback.complete(data);
    }

    // Method to handle Integer data, extract it from the buffer and pass it to the callback
    private void handleInt(ByteBuffer buffer, Callback callback) {
        int data = buffer.getInt();
        callback.complete(data);
    }

    // Method to handle Float data, extract it from the buffer and pass it to the callback
    private void handleFloat(ByteBuffer buffer, Callback callback) {
        float data = buffer.getFloat();
        callback.complete(data);
    }

    // Method to handle Double data, extract it from the buffer and pass it to the callback
    private void handleDouble(ByteBuffer buffer, Callback callback) {
        double data = buffer.getDouble();
        callback.complete(data);
    }

    // Method to handle Character data, extract it from the buffer and pass it to the callback
    private void handleChar(ByteBuffer buffer, Callback callback) {
        char data = buffer.getChar();
        callback.complete(data);
    }

    // Method to handle Byte Array data, extract it from the buffer and pass it to the callback
    private void handleByteArray(ByteBuffer buffer, Callback callback) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        callback.complete(data);
    }
}
