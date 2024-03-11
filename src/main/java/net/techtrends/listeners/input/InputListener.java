package net.techtrends.listeners.input;

import net.techtrends.exception.MaxBufferSizeExceededException;
import net.techtrends.listeners.output.AsyncOutputSocket;
import net.techtrends.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

public class InputListener implements CompletionHandler<Integer, ByteBuffer> {
    private static AsynchronousSocketChannel socketChannel;
    private final int maxBufferSize;
    private final Callback callback;
    private ByteBuffer inputBuffer;

    public InputListener(AsynchronousSocketChannel socketChannel, boolean allocateDirect, int initialBufferSize, int maxBufferSize, Callback callback) {
        InputListener.socketChannel = socketChannel;
        this.callback = callback;
        this.maxBufferSize = maxBufferSize;
        if (allocateDirect) {
            this.inputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.inputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
    }


    public void start() {
        socketChannel.read(inputBuffer, inputBuffer, this);
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {
        if (bytesRead < 0) {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
            return;
        }
        if (bytesRead > 0) {

            if (buffer.remaining() < maxBufferSize && buffer.capacity() < maxBufferSize) {
                double bufferUsagePercentage = (double) buffer.position() / buffer.capacity();
                if (bufferUsagePercentage >= 0.75) {

                    ByteBuffer newBuffer = buffer.isDirect() ? ByteBuffer.allocateDirect(Math.min(buffer.capacity() * 2, maxBufferSize)) : ByteBuffer.allocate(Math.min(buffer.capacity() * 2, maxBufferSize));
                    buffer.flip();
                    newBuffer.put(buffer);
                    inputBuffer = newBuffer;
                }
            } else {
                AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                try {
                    throw new MaxBufferSizeExceededException();
                } catch (MaxBufferSizeExceededException e) {
                    e.printStackTrace();
                }
            }


            buffer.flip();
            byte marker = buffer.get();
            if (marker >= 0x01 && marker <= 0x07) {

                switch (marker) {
                    case 0x01 -> handleString(buffer, callback);
                    case 0x02 -> handleInt(buffer, callback);
                    case 0x03 -> handleFloat(buffer, callback);
                    case 0x04 -> handleDouble(buffer, callback);
                    case 0x05 -> handleChar(buffer, callback);
                    case 0x06 -> handleByteArray(buffer, callback);
                    case 0x07 -> handleStringSanitized(buffer, callback);
                }
            } else {
                AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                System.out.println("Invalid marker received!");
            }
            buffer.clear();
            inputBuffer.clear();
        }
        socketChannel.read(inputBuffer, inputBuffer, this);
    }

    private void handleString(ByteBuffer buffer, Callback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        callback.complete(data);

    }

    private void handleStringSanitized(ByteBuffer buffer, Callback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        String sanitizedData = sanitizeString(data);
        callback.complete(sanitizedData);
    }

    private String sanitizeString(String data) {
        String allowedCharactersRegex = "[^A-Za-z0-9]+";
        return data.replaceAll(allowedCharactersRegex, "");
    }

    private void handleInt(ByteBuffer buffer, Callback callback) {
        int data = buffer.getInt();
        callback.complete(data);
    }

    private void handleFloat(ByteBuffer buffer, Callback callback) {
        float data = buffer.getFloat();
        callback.complete(data);
    }

    private void handleDouble(ByteBuffer buffer, Callback callback) {
        double data = buffer.getDouble();
        callback.complete(data);
    }

    private void handleChar(ByteBuffer buffer, Callback callback) {
        char data = buffer.getChar();
        callback.complete(data);
    }

    private void handleByteArray(ByteBuffer buffer, Callback callback) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        callback.complete(data);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }

    public void close() {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }
}
