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
//TODO ADD END FOR A PACKET, ONLY SENDSTRING HAS THIS
public class DataEndoder implements CompletionHandler<Integer, Callback> {

    private final AsynchronousSocketChannel socketChannel; // The socket channel for sending data
    private ByteBuffer outputBuffer; // Buffer to hold data to be sent
    private final boolean allocateDirect;
    private final boolean performResizing;
    private Callback currentCallback;

    // Constructor to initialize the OutputListener with a socket channel, buffer size, and allocation type
    public DataEndoder(AsynchronousSocketChannel socketChannel, int initialBufferSize, boolean allocateDirect, boolean performResizing) {
        this.socketChannel = socketChannel;
        // Allocate buffer either as direct or non-direct based on the flag
        if (allocateDirect) {
            this.outputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.outputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
        this.allocateDirect = allocateDirect;
        this.performResizing = performResizing;
    }

    // Method to send an integer value through the socket channel
    public void sendInt(int data, Callback callback) {
        byte marker = 0x02; // Marker to indicate the type of data
        int dataSize = 1 + Integer.BYTES;

        // Check if data size exceeds buffer capacity
        prepareByteBuffer(dataSize);

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
        byte endMarker = 0x00;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int dataSize = 2 + bytes.length;

        prepareByteBuffer(dataSize);

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(bytes);
        outputBuffer.put(endMarker);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to send a float value through the socket channel
    public void sendFloat(float data, Callback callback) {
        byte marker = 0x03; // Marker for float data
        int dataSize = Float.BYTES;

        // Check if data size exceeds buffer capacity
        prepareByteBuffer(dataSize);

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
        int dataSize = 1 + Double.BYTES;

        // Check if data size exceeds buffer capacity
        prepareByteBuffer(dataSize);

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
        int dataSize = 1 + Character.BYTES;

        // Check if data size exceeds buffer capacity
        prepareByteBuffer(dataSize);

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
        int dataSize = 1 + data.length;

        // Check if data size exceeds buffer capacity
        prepareByteBuffer(dataSize);

        // Prepare buffer to send data
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(data);
        outputBuffer.flip();

        performSend(callback); // Send the data
    }

    // Method to initiate the asynchronous write operation
    private void writeOutputBuffer() {
        socketChannel.write(outputBuffer, currentCallback, this);
    }

    private void prepareByteBuffer(int dataSize){
        if (dataSize > outputBuffer.capacity()) {
            if(!performResizing) {
                try {
                    throw new MaxBufferSizeExceededException(); // Throw exception if buffer is too small
                } catch (MaxBufferSizeExceededException e) {
                    throw new RuntimeException(e);
                }
            }else{
                int newSize = Math.max(outputBuffer.capacity() * 2, dataSize);
                if (allocateDirect) {
                    this.outputBuffer = ByteBuffer.allocateDirect(newSize);
                } else {
                    this.outputBuffer = ByteBuffer.allocate(newSize);
                }
            }
        }
    }

    @Override
    public void completed(Integer bytesWritten, Callback callback) {
        // This method is called when data is successfully written to the socket channel

        if (bytesWritten < 0) {
            return;
        }

        // If there are still bytes remaining in the buffer, continue writing
        if (outputBuffer.hasRemaining()) {
            socketChannel.write(outputBuffer, callback, this);
        }
        this.currentCallback = null;

    }

    @Override
    public void failed(Throwable exc, Callback attachmentCallback) {
        System.err.println("Error while sending data: " + exc.getMessage());
        exc.printStackTrace();
        AsyncChannelSocket.closeChannelSocketChannel(socketChannel); // Close the channel on failure

        // Notify the original callback about the failure with the exception.
        if (attachmentCallback != null) { // Use the attachmentCallback directly
            attachmentCallback.completeExceptionally(exc);
        }
        // IMPORTANT: Clear currentCallback on failure as this is a final resolution for the send operation.
        this.currentCallback = null;
    }


    // Method to perform the send operation, including checks and initiating the write process
    private void performSend(Callback callback) {
        // If the socket channel is closed, complete the callback with a failure status
        if (!socketChannel.isOpen()) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
            callback.complete(false);
            return;
        }
        this.currentCallback = callback;

        writeOutputBuffer();
    }
}
