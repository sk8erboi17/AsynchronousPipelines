package it.sk8erboi17.listeners.output;

import it.sk8erboi17.exception.MaxBufferSizeExceededException;
import it.sk8erboi17.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;


/**
 * This class handles sending data from the server to the client.
 * It provides methods to send various types of data (e.g., integers, strings, floats, doubles, chars, byte arrays) over the AsynchronousSocketChannel.
 * Data is encoded into a ByteBuffer using a length-prefixed protocol and then sent asynchronously.
 */
public class DataEncoder implements CompletionHandler<Integer, Callback> {

    private static final byte START_MARKER = 0x01;

    private final AsynchronousSocketChannel socketChannel;
    private ByteBuffer outputBuffer;
    private final boolean allocateDirect;
    private final boolean performResizing;
    private Callback currentCallback;

    public DataEncoder(AsynchronousSocketChannel socketChannel, int initialBufferSize, boolean allocateDirect, boolean performResizing) {
        this.socketChannel = socketChannel;
        if (allocateDirect) {
            this.outputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.outputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
        this.allocateDirect = allocateDirect;
        this.performResizing = performResizing;
    }

    private void sendFrame(byte dataTypeMarker, byte[] payload, Callback callback) {
        int frameLength = 1 + payload.length;
        int totalPacketSize = 1 + Integer.BYTES + frameLength;

        prepareByteBuffer(totalPacketSize);

        outputBuffer.clear();
        outputBuffer.put(START_MARKER);
        outputBuffer.putInt(frameLength);
        outputBuffer.put(dataTypeMarker);
        outputBuffer.put(payload);
        outputBuffer.flip();

        performSend(callback);
    }

    public void sendInt(int data, Callback callback) {
        byte marker = 0x02;
        ByteBuffer tempBuffer = ByteBuffer.allocate(Integer.BYTES);
        tempBuffer.putInt(data);
        sendFrame(marker, tempBuffer.array(), callback);
    }

    public void sendString(String data, Callback callback) {
        byte marker = 0x01;
        byte[] stringBytes = data.getBytes(StandardCharsets.UTF_8);

        // contains [str_length] + [payload]
        ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + stringBytes.length);
        payload.putInt(stringBytes.length);
        payload.put(stringBytes);

        sendFrame(marker, payload.array(), callback);
    }

    public void sendFloat(float data, Callback callback) {
        byte marker = 0x03;
        ByteBuffer tempBuffer = ByteBuffer.allocate(Float.BYTES);
        tempBuffer.putFloat(data);
        sendFrame(marker, tempBuffer.array(), callback);
    }

    public void sendDouble(double data, Callback callback) {
        byte marker = 0x04;
        ByteBuffer tempBuffer = ByteBuffer.allocate(Double.BYTES);
        tempBuffer.putDouble(data);
        sendFrame(marker, tempBuffer.array(), callback);
    }

    public void sendChar(char data, Callback callback) {
        byte marker = 0x05;
        ByteBuffer tempBuffer = ByteBuffer.allocate(Character.BYTES);
        tempBuffer.putChar(data);
        sendFrame(marker, tempBuffer.array(), callback);
    }

    public void sendByteArray(byte[] data, Callback callback) {
        byte marker = 0x06;

        ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES + data.length);
        payload.putInt(data.length);
        payload.put(data);

        sendFrame(marker, payload.array(), callback);
    }

    private void prepareByteBuffer(int dataSize) {
        /*
        FIND A BETTER SOLUTION
        if (dataSize > MAX_BUFFER_SIZE) {
         failed(new MaxBufferSizeExceededException("Requested data size " + dataSize + " exceeds max limit " + MAX_BUFFER_SIZE), currentCallback);
         return;
    }
         */
        if (dataSize > outputBuffer.capacity()) {
            if (!performResizing) {
                try {
                    throw new MaxBufferSizeExceededException();
                } catch (MaxBufferSizeExceededException e) {
                    if (currentCallback != null) {
                        currentCallback.completeExceptionally(e);
                    }
                    throw new RuntimeException(e); // TODO BETER HANDLER
                }
            } else {
                int newSize = Math.max(outputBuffer.capacity() * 2, dataSize);
                ByteBuffer newBuffer;
                if (allocateDirect) {
                    newBuffer = ByteBuffer.allocateDirect(newSize);
                } else {
                    newBuffer = ByteBuffer.allocate(newSize);
                }
                outputBuffer = newBuffer;
            }
        }
    }

    private void performSend(Callback callback) {
        if (!socketChannel.isOpen()) {
            if (callback != null) {
                callback.completeExceptionally(new java.nio.channels.ClosedChannelException());
            }
            return;
        }
        this.currentCallback = callback;
        writeOutputBuffer();
    }

    private void writeOutputBuffer() {
        socketChannel.write(outputBuffer, currentCallback, this);
    }

    @Override
    public void completed(Integer bytesWritten, Callback callback) {
        if (bytesWritten < 0) {
            failed(new java.io.EOFException("Client disconnected"), callback);
            return;
        }
        if (outputBuffer.hasRemaining()) {
            socketChannel.write(outputBuffer, callback, this);
        } else {
            this.currentCallback = null;
        }
    }

    @Override
    public void failed(Throwable exc, Callback attachmentCallback) {
        System.err.println("Error while sending data: " + exc.getMessage());
        AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
        if (attachmentCallback != null) {
            attachmentCallback.completeExceptionally(exc);
        }
        this.currentCallback = null;
    }
}

