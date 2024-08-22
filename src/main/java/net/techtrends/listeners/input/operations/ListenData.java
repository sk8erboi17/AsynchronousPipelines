package net.techtrends.listeners.input.operations;

import net.techtrends.listeners.response.Callback;
import net.techtrends.network.AggregateCallback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The ListenData class is responsible for processing data read from a ByteBuffer.
 * It identifies the type of data based on an initial marker byte and then decodes the data accordingly.
 * The class uses a Callback to handle the processed data or report errors.
 */
public class ListenData {

<<<<<<< HEAD
    public void listen(ByteBuffer buffer, Callback callback) {
        // If the buffer has no remaining bytes, return immediately
=======
    public void listen(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        if (buffer.remaining() <= 0) {
            return;
        }

        // Read the first byte to determine the type of data in the buffer
        byte marker = buffer.get();

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
                String data = StandardCharsets.UTF_8.decode(buffer).toString();
                System.err.println("Error to string: " + data);
                throw new RuntimeException("Error with buffer, do you have enough space?");
            } catch (ClassCastException e) {
                throw new RuntimeException("Error with buffer, do you have enough space?" + e.getMessage(), e);
            }
        }
    }

<<<<<<< HEAD
    // Method to handle String data, decode it from the buffer and pass it to the callback
    private void handleString(ByteBuffer buffer, Callback callback) {
=======
    private void handleString(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        String data = StandardCharsets.UTF_8.decode(buffer).toString();

        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }

    }

<<<<<<< HEAD
    // Method to handle Integer data, extract it from the buffer and pass it to the callback
    private void handleInt(ByteBuffer buffer, Callback callback) {
=======
    private void handleInt(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        int data = buffer.getInt();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

<<<<<<< HEAD
    // Method to handle Float data, extract it from the buffer and pass it to the callback
    private void handleFloat(ByteBuffer buffer, Callback callback) {
=======
    private void handleFloat(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        float data = buffer.getFloat();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

<<<<<<< HEAD
    // Method to handle Double data, extract it from the buffer and pass it to the callback
    private void handleDouble(ByteBuffer buffer, Callback callback) {
=======
    private void handleDouble(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        double data = buffer.getDouble();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

<<<<<<< HEAD
    // Method to handle Character data, extract it from the buffer and pass it to the callback
    private void handleChar(ByteBuffer buffer, Callback callback) {
=======
    private void handleChar(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        char data = buffer.getChar();
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }

<<<<<<< HEAD
    // Method to handle Byte Array data, extract it from the buffer and pass it to the callback
    private void handleByteArray(ByteBuffer buffer, Callback callback) {
=======
    private void handleByteArray(ByteBuffer buffer, AggregateCallback callback) {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        for (Callback iteratedCallback : callback.getCallbacks()) {
            iteratedCallback.complete(data);
        }
    }
}
