package net.techtrends.general.listeners.input;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.listeners.ResponseCallback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * The InputListener class is responsible for handling input events and receiving
 * data through an AsynchronousSocketChannel. It implements the CompletionHandler
 * interface to provide a way to process and receive data from the channel.
 * This class maintains a ByteBuffer for incoming data and provides a method to read
 * the data when it is available.
 */
public class InputListener implements CompletionHandler<Integer, ByteBuffer> {
    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffer inputBuffer;
    private final ResponseCallback responseCallback;

    public InputListener(AsynchronousSocketChannel socketChannel, boolean allocateDirect, ResponseCallback responseCallback) {
        this.socketChannel = socketChannel;
        this.responseCallback = responseCallback;
        if (allocateDirect) {
            this.inputBuffer = ByteBuffer.allocateDirect(2048);
        } else {
            this.inputBuffer = ByteBuffer.allocate(2048);
        }
    }

    public void start() {
        socketChannel.read(inputBuffer, inputBuffer, this);
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {
        if (bytesRead < 0) {
            return;
        }
        if (bytesRead > 0) {
            buffer.flip();

            byte marker = buffer.get();

            switch (marker) {
                case 0x01 -> handleString(buffer, responseCallback);
                case 0x02 -> handleInt(buffer, responseCallback);
                case 0x03 -> handleFloat(buffer, responseCallback);
                case 0x04 -> handleDouble(buffer, responseCallback);
                case 0x05 -> handleChar(buffer, responseCallback);
                case 0x06 -> handleByteArray(buffer, responseCallback);
                default -> AsyncSocket.closeSocketChannel(socketChannel);
            }
            buffer.clear();
        }
        socketChannel.read(buffer, buffer, this);
    }

    private void handleString(ByteBuffer buffer, ResponseCallback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        CompletableFuture.runAsync(() -> callback.complete(data));
    }

    private void handleInt(ByteBuffer buffer, ResponseCallback callback) {
        int data = buffer.getInt();
        CompletableFuture.runAsync(() -> callback.complete(data));
    }

    private void handleFloat(ByteBuffer buffer, ResponseCallback callback) {
        float data = buffer.getFloat();
        CompletableFuture.runAsync(() -> callback.complete(data));
    }

    private void handleDouble(ByteBuffer buffer, ResponseCallback callback) {
        double data = buffer.getDouble();
        CompletableFuture.runAsync(() -> callback.complete(data));
    }

    private void handleChar(ByteBuffer buffer, ResponseCallback callback) {
        char data = buffer.getChar();
        CompletableFuture.runAsync(() -> callback.complete(data));
    }

    private void handleByteArray(ByteBuffer buffer, ResponseCallback callback) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        CompletableFuture.runAsync(() -> callback.complete(data));
    }


    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        AsyncSocket.closeSocketChannel(socketChannel);
        System.out.println("Client disconnect!");
    }
}
