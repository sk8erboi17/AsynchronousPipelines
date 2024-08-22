package net.techtrends.listeners.output;

import net.techtrends.exception.MaxBufferSizeExceededException;
import net.techtrends.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * This class handles sending data from the server to the client.
 * It provides methods to send various types of data (e.g., integers, strings, floats, doubles, chars, byte arrays) over the AsynchronousSocketChannel.
 * Data is encoded into a ByteBuffer and then sent asynchronously.
 */
public class AsyncDataSender implements CompletionHandler<Integer, ByteBuffer> {

    private final AsynchronousSocketChannel socketChannel; // The socket channel for sending data
    private final ByteBuffer outputBuffer; // Buffer to hold data to be sent

    // Constructor to initialize the OutputListener with a socket channel, buffer size, and allocation type
    public AsyncDataSender(AsynchronousSocketChannel socketChannel, int initialBufferSize, boolean allocateDirect) {
        this.socketChannel = socketChannel;
        // Allocate buffer either as direct or non-direct based on the flag
        if (allocateDirect) {
            this.outputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.outputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
    }

    // Method to send an integer value through the socket channel
    public void sendInt(int data, Callback callback) {
        byte marker = 0x02; // Marker to indicate the type of data
        int dataSize = Integer.BYTES;

        // Check if data size exceeds buffer capacity
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putInt(data);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to send a string through the socket channel
    public void sendString(String data, Callback callback) {
        byte marker = 0x01; // Marker for string data
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int dataSize = bytes.length + 1;

        // Check if data size exceeds buffer capacity
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(bytes);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to send a float value through the socket channel
    public void sendFloat(float data, Callback callback) {
        byte marker = 0x03; // Marker for float data
        int dataSize = Float.BYTES;

        // Check if data size exceeds buffer capacity
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putFloat(data);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to send a double value through the socket channel
    public void sendDouble(double data, Callback callback) {
        byte marker = 0x04; // Marker for double data
        int dataSize = Double.BYTES;

        // Check if data size exceeds buffer capacity
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putDouble(data);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to send a char value through the socket channel
    public void sendChar(char data, Callback callback) {
        byte marker = 0x05; // Marker for char data
        int dataSize = Character.BYTES;

        // Check if data size exceeds buffer capacity
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putChar(data);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to send a byte array through the socket channel
    public void sendByteArray(byte[] data, Callback callback) {
        byte marker = 0x06; // Marker for byte array data
        int dataSize = data.length + 1;

        // Check if data size exceeds buffer capacity
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(data);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to initiate the asynchronous write operation
    private void writeOutputBuffer() {
        CompletableFuture.runAsync(() -> socketChannel.write(outputBuffer, outputBuffer, this));
    }

    @Override
    public void completed(Integer bytesWritten, ByteBuffer buffer) {
        // This method is called when data is successfully written to the socket channel

        if (bytesWritten < 0) {
            return;
        }

        // If there are still bytes remaining in the buffer, continue writing
        if (buffer.hasRemaining()) {
            socketChannel.write(buffer, buffer, this);
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        // This method is called if the write operation fails
        System.err.println("Error while sending data: " + exc.getMessage());
        exc.printStackTrace();
        AsyncChannelSocket.closeChannelSocketChannel(socketChannel); // Close the channel on failure
    }

    // Method to perform the send operation, including checks and initiating the write process
    private void performSend(Callback callback) {
        // If the socket channel is closed, complete the callback with a failure status
        if (!socketChannel.isOpen()) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
            callback.complete(false);
            return;
        }

        writeOutputBuffer(); // Initiate the asynchronous write operation
    }
}
